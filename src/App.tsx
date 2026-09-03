import React, { useState, useEffect, useMemo } from 'react';
import JSZip from 'jszip';
import confetti from 'canvas-confetti';
import { 
  Download, 
  Copy, 
  Check, 
  FileCode, 
  Smartphone, 
  FolderTree, 
  Terminal, 
  BookOpen, 
  CheckCircle2, 
  AlertCircle,
  ExternalLink,
  ChevronRight,
  Clock,
  Sparkles,
  RefreshCw,
  Plus,
  Trash2,
  Edit2,
  Calendar,
  Layers,
  ArrowRight
} from 'lucide-react';
import { PROJECT_FILES, ProjectFile } from './projectFiles';

// --- Domain Models ---
export type Subject = 'Maths' | 'Physics' | 'Chemistry' | 'English';
export const SUBJECTS: Subject[] = ['Maths', 'Physics', 'Chemistry', 'English'];
export const DEFAULT_INTERVALS = [3, 7, 14, 21, 30, 45, 60, 90, 120, 180, 365];

export interface Topic {
  id: number;
  subject: Subject;
  title: string;
  chapter: string | null;
  createdAt: number;
  revisionHour: number;
  revisionMinute: number;
  intervals: number[];
}

export interface Revision {
  id: number;
  topicId: number;
  intervalIndex: number;
  intervalDays: number;
  dueAt: number;
  alertAt: number;
  status: 'SCHEDULED' | 'DONE' | 'MISSED';
  completedAt: number | null;
}

// --- Scheduler Pure Functions (matches Kotlin RevisionScheduler) ---
function parseIntervals(text: string): number[] {
  return text
    .split(',')
    .map(t => parseInt(t.trim(), 10))
    .filter(n => !isNaN(n) && n > 0);
}

function baseTimestamp(anchorMillis: number, hour: number, minute: number): number {
  const d = new Date(anchorMillis);
  const target = new Date(d.getFullYear(), d.getMonth(), d.getDate(), hour, minute, 0, 0);
  if (target.getTime() <= anchorMillis) {
    target.setDate(target.getDate() + 1);
  }
  return target.getTime();
}

function buildRevisions(
  topicId: number,
  baseMillis: number,
  intervals: number[],
  nowMillis: number
): Omit<Revision, 'id'>[] {
  const revisions: Omit<Revision, 'id'>[] = [];
  const baseDate = new Date(baseMillis);

  intervals.forEach((days, idx) => {
    const dueDate = new Date(baseDate);
    dueDate.setDate(dueDate.getDate() + days);
    const dueAt = dueDate.getTime();

    if (dueAt > nowMillis) {
      revisions.push({
        topicId,
        intervalIndex: idx,
        intervalDays: days,
        dueAt,
        alertAt: dueAt - 120000,
        status: 'SCHEDULED',
        completedAt: null
      });
    }
  });

  return revisions;
}

function previewTimestamps(nowMillis: number, hour: number, minute: number, intervals: number[]): number[] {
  const base = baseTimestamp(nowMillis, hour, minute);
  const baseDate = new Date(base);
  return intervals.map(days => {
    const d = new Date(baseDate);
    d.setDate(d.getDate() + days);
    return d.getTime();
  });
}

function formatDateTime(millis: number): string {
  const d = new Date(millis);
  const day = String(d.getDate()).padStart(2, '0');
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const month = monthNames[d.getMonth()];
  const year = d.getFullYear();
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${day} ${month} ${year}, ${hours}:${minutes}`;
}

function isSameDay(aMillis: number, bMillis: number): boolean {
  const a = new Date(aMillis);
  const b = new Date(bMillis);
  return a.getFullYear() === b.getFullYear() &&
         a.getMonth() === b.getMonth() &&
         a.getDate() === b.getDate();
}

export default function App() {
  // Navigation mode: 'interactive' or 'files' or 'guide'
  const [appMode, setAppMode] = useState<'interactive' | 'files' | 'guide'>('interactive');

  // Interactive Android App State (Starts at 0 topics, 0 revisions, fresh install)
  const [topics, setTopics] = useState<Topic[]>(() => {
    const saved = localStorage.getItem('etea_blank_v1_topics');
    return saved ? JSON.parse(saved) : [];
  });
  const [revisions, setRevisions] = useState<Revision[]>(() => {
    const saved = localStorage.getItem('etea_blank_v1_revisions');
    return saved ? JSON.parse(saved) : [];
  });

  // Simulated Current Time (allows testing future / missed dates)
  const [virtualNow, setVirtualNow] = useState<number>(Date.now());

  // Android Tab state (0: Home, 1: Revise, 2: Subjects, 3: All)
  const [activeTab, setActiveTab] = useState<number>(0);

  // BottomSheet State
  const [showSheet, setShowSheet] = useState<boolean>(false);
  const [editingTopic, setEditingTopic] = useState<Topic | null>(null);
  const [sheetSubject, setSheetSubject] = useState<Subject>('Maths');
  const [sheetTitle, setSheetTitle] = useState<string>('');
  const [sheetChapter, setSheetChapter] = useState<string>('');
  const [sheetHour, setSheetHour] = useState<number>(18);
  const [sheetMinute, setSheetMinute] = useState<number>(30);
  const [sheetIntervalsText, setSheetIntervalsText] = useState<string>(DEFAULT_INTERVALS.join(','));

  // Subjects tab filter
  const [selectedSubjectFilter, setSelectedSubjectFilter] = useState<Subject>('Maths');

  // File browser state
  const [selectedFilePath, setSelectedFilePath] = useState<string>('app/src/main/java/com/umairshahab/etea/studyplan/MainActivity.kt');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [copiedFile, setCopiedFile] = useState<string | null>(null);
  const [isZipping, setIsZipping] = useState<boolean>(false);

  // Save to localStorage
  useEffect(() => {
    localStorage.setItem('etea_blank_v1_topics', JSON.stringify(topics));
  }, [topics]);

  useEffect(() => {
    localStorage.setItem('etea_blank_v1_revisions', JSON.stringify(revisions));
  }, [revisions]);

  // Derived intervals for sheet
  const parsedSheetIntervals = useMemo(() => {
    return parseIntervals(sheetIntervalsText);
  }, [sheetIntervalsText]);

  const previewList = useMemo(() => {
    if (parsedSheetIntervals.length === 0) return [];
    return previewTimestamps(virtualNow, sheetHour, sheetMinute, parsedSheetIntervals);
  }, [virtualNow, sheetHour, sheetMinute, parsedSheetIntervals]);

  // Topic Actions
  const handleOpenAdd = (subject: Subject = 'Maths') => {
    setEditingTopic(null);
    setSheetSubject(subject);
    setSheetTitle('');
    setSheetChapter('');
    setSheetHour(18);
    setSheetMinute(30);
    setSheetIntervalsText(DEFAULT_INTERVALS.join(','));
    setShowSheet(true);
  };

  const handleOpenEdit = (topic: Topic) => {
    setEditingTopic(topic);
    setSheetSubject(topic.subject);
    setSheetTitle(topic.title);
    setSheetChapter(topic.chapter || '');
    setSheetHour(topic.revisionHour);
    setSheetMinute(topic.revisionMinute);
    setSheetIntervalsText(topic.intervals.join(','));
    setShowSheet(true);
  };

  const handleSaveTopic = () => {
    if (!sheetTitle.trim() || parsedSheetIntervals.length === 0) return;

    if (editingTopic === null) {
      // Add new topic
      const newId = Date.now();
      const newTopic: Topic = {
        id: newId,
        subject: sheetSubject,
        title: sheetTitle.trim(),
        chapter: sheetChapter.trim() || null,
        createdAt: virtualNow,
        revisionHour: sheetHour,
        revisionMinute: sheetMinute,
        intervals: parsedSheetIntervals
      };

      const base = baseTimestamp(virtualNow, sheetHour, sheetMinute);
      const newRevs = buildRevisions(newId, base, parsedSheetIntervals, virtualNow);
      
      const revsWithId: Revision[] = newRevs.map((r, i) => ({
        ...r,
        id: newId + 100 + i
      }));

      setTopics(prev => [newTopic, ...prev]);
      setRevisions(prev => [...prev, ...revsWithId]);
      confetti({ particleCount: 40, spread: 60, origin: { y: 0.8 } });
    } else {
      // Edit topic
      const topicId = editingTopic.id;
      const updatedTopic: Topic = {
        ...editingTopic,
        subject: sheetSubject,
        title: sheetTitle.trim(),
        chapter: sheetChapter.trim() || null,
        revisionHour: sheetHour,
        revisionMinute: sheetMinute,
        intervals: parsedSheetIntervals
      };

      // Keep completed revision history, delete only SCHEDULED
      const keptRevs = revisions.filter(r => !(r.topicId === topicId && r.status === 'SCHEDULED'));

      // Regenerate future revisions from original createdAt base
      const base = baseTimestamp(editingTopic.createdAt, sheetHour, sheetMinute);
      const regenerated = buildRevisions(topicId, base, parsedSheetIntervals, virtualNow);
      const regenWithId: Revision[] = regenerated.map((r, i) => ({
        ...r,
        id: Date.now() + 500 + i
      }));

      setTopics(prev => prev.map(t => t.id === topicId ? updatedTopic : t));
      setRevisions([...keptRevs, ...regenWithId]);
    }

    setShowSheet(false);
  };

  const handleDeleteTopic = (topicId: number) => {
    // Delete topic and ALL its revisions
    setTopics(prev => prev.filter(t => t.id !== topicId));
    setRevisions(prev => prev.filter(r => r.topicId !== topicId));
  };

  const handleMarkDone = (revId: number) => {
    setRevisions(prev => prev.map(r => {
      if (r.id === revId) {
        return { ...r, status: 'DONE', completedAt: virtualNow };
      }
      return r;
    }));
    confetti({ particleCount: 30, spread: 50, origin: { y: 0.6 } });
  };

  const handleResetDatabase = () => {
    if (window.confirm('Reset Room database (etea_blank_v1) to 0 topics and 0 revisions?')) {
      setTopics([]);
      setRevisions([]);
      localStorage.removeItem('etea_blank_v1_topics');
      localStorage.removeItem('etea_blank_v1_revisions');
    }
  };

  // ZIP Downloader
  const handleDownloadZip = async () => {
    setIsZipping(true);
    try {
      const zip = new JSZip();
      PROJECT_FILES.forEach(file => {
        zip.file(file.path, file.content);
      });

      const blob = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'etea-study-plan-android.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error generating zip:', err);
    } finally {
      setIsZipping(false);
    }
  };

  const handleCopyCode = (content: string, path: string) => {
    navigator.clipboard.writeText(content);
    setCopiedFile(path);
    setTimeout(() => setCopiedFile(null), 2000);
  };

  // Revision queues for Revise tab
  const topicMap = useMemo(() => {
    const map = new Map<number, Topic>();
    topics.forEach(t => map.set(t.id, t));
    return map;
  }, [topics]);

  const dueTodayRevisions = useMemo(() => {
    return revisions.filter(
      r => r.status === 'SCHEDULED' && isSameDay(r.dueAt, virtualNow) && r.dueAt >= virtualNow
    );
  }, [revisions, virtualNow]);

  const missedRevisions = useMemo(() => {
    return revisions.filter(
      r => r.status === 'SCHEDULED' && r.dueAt < virtualNow
    );
  }, [revisions, virtualNow]);

  const upcomingRevisions = useMemo(() => {
    return revisions
      .filter(r => r.status === 'SCHEDULED' && r.dueAt >= virtualNow && !isSameDay(r.dueAt, virtualNow))
      .sort((a, b) => a.dueAt - b.dueAt)
      .slice(0, 10);
  }, [revisions, virtualNow]);

  const filteredTopics = useMemo(() => {
    return topics.filter(t => t.subject === selectedSubjectFilter);
  }, [topics, selectedSubjectFilter]);

  const selectedFile = useMemo(() => {
    return PROJECT_FILES.find(f => f.path === selectedFilePath) || PROJECT_FILES[0];
  }, [selectedFilePath]);

  const filteredProjectFiles = useMemo(() => {
    if (!searchQuery.trim()) return PROJECT_FILES;
    const q = searchQuery.toLowerCase();
    return PROJECT_FILES.filter(f => f.path.toLowerCase().includes(q) || f.description.toLowerCase().includes(q));
  }, [searchQuery]);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Top Header */}
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur sticky top-0 z-30 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center font-black text-white text-lg shadow-md shadow-blue-500/20">
            SP
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="font-bold text-slate-100 text-base leading-tight">Study Plan</h1>
              <span className="text-[10px] font-mono bg-blue-500/10 text-blue-400 border border-blue-500/20 px-1.5 py-0.5 rounded">
                v1.0 • Room etea_blank_v1
              </span>
            </div>
            <p className="text-xs text-slate-400">Jetpack Compose + Material3 • Android 14 (SDK 34)</p>
          </div>
        </div>

        {/* View Switcher Tabs */}
        <div className="flex items-center space-x-2 bg-slate-800/80 p-1 rounded-xl border border-slate-700">
          <button
            id="nav-interactive-tab"
            onClick={() => setAppMode('interactive')}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              appMode === 'interactive'
                ? 'bg-blue-600 text-white shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            <span>Live Android App</span>
          </button>

          <button
            id="nav-files-tab"
            onClick={() => setAppMode('files')}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              appMode === 'files'
                ? 'bg-blue-600 text-white shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <FolderTree className="w-3.5 h-3.5" />
            <span>Files ({PROJECT_FILES.length})</span>
          </button>

          <button
            id="nav-guide-tab"
            onClick={() => setAppMode('guide')}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              appMode === 'guide'
                ? 'bg-blue-600 text-white shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Terminal className="w-3.5 h-3.5" />
            <span>GitHub Actions Setup</span>
          </button>
        </div>

        {/* Download Zip Action */}
        <button
          id="btn-download-all-zip"
          onClick={handleDownloadZip}
          disabled={isZipping}
          className="flex items-center space-x-1.5 bg-emerald-600 hover:bg-emerald-500 text-white px-3.5 py-1.5 rounded-xl text-xs font-semibold shadow transition cursor-pointer disabled:opacity-50"
        >
          <Download className="w-3.5 h-3.5" />
          <span>{isZipping ? 'Archiving...' : 'Download Project (.ZIP)'}</span>
        </button>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 flex overflow-hidden">
        {appMode === 'interactive' && (
          <div className="flex-1 flex flex-col md:flex-row items-center justify-center p-4 gap-6 bg-slate-950 overflow-y-auto">
            {/* Phone Shell */}
            <div className="w-full max-w-[390px] h-[780px] bg-white text-slate-900 rounded-[44px] shadow-2xl shadow-blue-900/20 border-8 border-slate-800 flex flex-col overflow-hidden relative select-none">
              
              {/* Phone Speaker & Camera Notch */}
              <div className="h-7 bg-white flex items-center justify-between px-6 pt-1">
                <span className="text-[11px] font-semibold text-slate-700">
                  {new Date(virtualNow).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
                <div className="w-20 h-4 bg-slate-900 rounded-full flex items-center justify-center">
                  <div className="w-2 h-2 rounded-full bg-slate-800 mr-2" />
                  <div className="w-2.5 h-2.5 rounded-full bg-blue-950/80" />
                </div>
                <div className="flex items-center space-x-1 text-[10px] text-slate-700">
                  <span>5G</span>
                  <span>100%</span>
                </div>
              </div>

              {/* Android Top Title / App Bar */}
              <div className="flex-1 flex flex-col bg-slate-50 overflow-y-auto">
                
                {/* TAB 0: HOME SCREEN */}
                {activeTab === 0 && (
                  <div className="p-4 space-y-4">
                    {/* Header: SP Logo + Study Plan */}
                    <div className="flex items-center space-x-3 pt-2">
                      <span className="text-4xl font-black bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                        SP
                      </span>
                      <div>
                        <h2 className="text-xl font-bold text-slate-900 leading-tight">Study Plan</h2>
                      </div>
                    </div>

                    {/* Metric Cards Row */}
                    <div className="grid grid-cols-3 gap-2">
                      <div className="bg-blue-50 border border-blue-100 rounded-2xl p-3 text-center">
                        <div className="text-2xl font-bold text-blue-700">{topics.length}</div>
                        <div className="text-xs font-medium text-blue-600">Topics</div>
                      </div>
                      <div className="bg-emerald-50 border border-emerald-100 rounded-2xl p-3 text-center">
                        <div className="text-2xl font-bold text-emerald-700">{dueTodayRevisions.length}</div>
                        <div className="text-xs font-medium text-emerald-600">Today</div>
                      </div>
                      <div className="bg-rose-50 border border-rose-100 rounded-2xl p-3 text-center">
                        <div className="text-2xl font-bold text-rose-700">{missedRevisions.length}</div>
                        <div className="text-xs font-medium text-rose-600">Missed</div>
                      </div>
                    </div>

                    {/* Action Row */}
                    <div className="flex items-center justify-between pt-1">
                      <span className="text-sm font-semibold text-slate-600">
                        Total {topics.length}
                      </span>
                      <button
                        id="btn-add-topic-home"
                        onClick={() => handleOpenAdd()}
                        className="bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold px-3.5 py-2 rounded-full shadow-sm flex items-center space-x-1 cursor-pointer"
                      >
                        <Plus className="w-3.5 h-3.5" />
                        <span>Add Topic</span>
                      </button>
                    </div>

                    {/* Calendar Placeholder Card */}
                    <div className="bg-slate-100 border border-slate-200 rounded-2xl p-3.5 flex items-center space-x-3">
                      <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-lg shrink-0">
                        📅
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-slate-800">Calendar View</div>
                        <div className="text-xs text-slate-500 leading-tight">
                          Calendar arrives later. Track daily targets right here!
                        </div>
                      </div>
                    </div>

                    {/* Topics List or Empty State */}
                    {topics.length === 0 ? (
                      <div className="py-10 text-center space-y-2">
                        <div className="text-4xl">📚</div>
                        <h3 className="text-base font-bold text-slate-800">No topics yet</h3>
                        <p className="text-xs text-slate-500 max-w-[220px] mx-auto">
                          Tap "+ Add Topic" above to create your first study target. Fresh install starts at 0.
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-2.5">
                        <h3 className="text-xs font-bold text-slate-600 uppercase tracking-wider">
                          Recent Topics
                        </h3>
                        {topics.slice(0, 4).map(topic => {
                          const nextRev = revisions.find(
                            r => r.topicId === topic.id && r.status === 'SCHEDULED' && r.dueAt >= virtualNow
                          );
                          return (
                            <div key={topic.id} className="bg-white border border-slate-200 rounded-xl p-3 shadow-xs">
                              <div className="flex items-start justify-between">
                                <div>
                                  <div className="font-bold text-sm text-slate-900">{topic.title}</div>
                                  <div className="text-xs text-slate-500 mt-0.5">
                                    {topic.chapter ? `${topic.chapter} • ` : ''}{topic.subject}
                                  </div>
                                  <div className="text-[11px] text-blue-600 font-medium mt-1">
                                    Next due: {nextRev ? formatDateTime(nextRev.dueAt) : 'All completed'}
                                  </div>
                                </div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                )}

                {/* TAB 1: REVISE SCREEN */}
                {activeTab === 1 && (
                  <div className="p-4 space-y-4">
                    <div>
                      <h2 className="text-xl font-bold text-slate-900">Revision Queue</h2>
                      <p className="text-xs text-slate-500">Static spaced repetition schedule</p>
                    </div>

                    {dueTodayRevisions.length === 0 && missedRevisions.length === 0 && upcomingRevisions.length === 0 ? (
                      <div className="py-12 text-center space-y-2">
                        <div className="text-4xl">⏰</div>
                        <h3 className="text-base font-bold text-slate-800">Nothing to revise</h3>
                        <p className="text-xs text-slate-500 max-w-[220px] mx-auto">
                          All revisions completed or no topics scheduled yet.
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {/* Due Today Section */}
                        {dueTodayRevisions.length > 0 && (
                          <div className="space-y-2">
                            <h3 className="text-xs font-bold text-emerald-700 uppercase tracking-wider flex items-center space-x-1">
                              <span>Due Today ({dueTodayRevisions.length})</span>
                            </h3>
                            {dueTodayRevisions.map(rev => {
                              const t = topicMap.get(rev.topicId);
                              return (
                                <div key={rev.id} className="bg-white border border-emerald-200 rounded-xl p-3 flex items-center justify-between shadow-xs">
                                  <div>
                                    <div className="text-sm font-semibold text-slate-900">{t?.title || `Topic #${rev.topicId}`}</div>
                                    <div className="text-xs text-emerald-600 font-medium">
                                      Due: {formatDateTime(rev.dueAt)}
                                    </div>
                                  </div>
                                  <button
                                    onClick={() => handleMarkDone(rev.id)}
                                    className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-3 py-1.5 rounded-full cursor-pointer"
                                  >
                                    Done
                                  </button>
                                </div>
                              );
                            })}
                          </div>
                        )}

                        {/* Missed Section */}
                        {missedRevisions.length > 0 && (
                          <div className="space-y-2">
                            <h3 className="text-xs font-bold text-rose-700 uppercase tracking-wider flex items-center space-x-1">
                              <span>Missed Revisions ({missedRevisions.length})</span>
                            </h3>
                            {missedRevisions.map(rev => {
                              const t = topicMap.get(rev.topicId);
                              return (
                                <div key={rev.id} className="bg-rose-50/50 border border-rose-200 rounded-xl p-3 flex items-center justify-between shadow-xs">
                                  <div>
                                    <div className="text-sm font-semibold text-slate-900">{t?.title || `Topic #${rev.topicId}`}</div>
                                    <div className="text-xs text-rose-600 font-medium">
                                      Missed: {formatDateTime(rev.dueAt)}
                                    </div>
                                  </div>
                                  <button
                                    onClick={() => handleMarkDone(rev.id)}
                                    className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold px-3 py-1.5 rounded-full cursor-pointer"
                                  >
                                    Done
                                  </button>
                                </div>
                              );
                            })}
                          </div>
                        )}

                        {/* Upcoming Section */}
                        {upcomingRevisions.length > 0 && (
                          <div className="space-y-2">
                            <h3 className="text-xs font-bold text-slate-600 uppercase tracking-wider">
                              Upcoming (Next 10)
                            </h3>
                            {upcomingRevisions.map(rev => {
                              const t = topicMap.get(rev.topicId);
                              return (
                                <div key={rev.id} className="bg-white border border-slate-200 rounded-xl p-3 flex items-center justify-between shadow-xs">
                                  <div>
                                    <div className="text-sm font-semibold text-slate-900">{t?.title || `Topic #${rev.topicId}`}</div>
                                    <div className="text-xs text-slate-500">
                                      {t?.subject} • Due {formatDateTime(rev.dueAt)}
                                    </div>
                                  </div>
                                  <button
                                    onClick={() => handleMarkDone(rev.id)}
                                    className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-3 py-1.5 rounded-full cursor-pointer"
                                  >
                                    Done
                                  </button>
                                </div>
                              );
                            })}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}

                {/* TAB 2: SUBJECTS SCREEN */}
                {activeTab === 2 && (
                  <div className="p-4 space-y-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <h2 className="text-xl font-bold text-slate-900">Subjects</h2>
                        <p className="text-xs text-slate-500">4 fixed subjects</p>
                      </div>
                      <button
                        onClick={() => handleOpenAdd(selectedSubjectFilter)}
                        className="bg-blue-600 text-white text-xs font-bold px-3 py-1.5 rounded-full cursor-pointer"
                      >
                        + Add
                      </button>
                    </div>

                    {/* Fixed Subject Chips */}
                    <div className="flex space-x-1.5 overflow-x-auto pb-1 no-scrollbar">
                      {SUBJECTS.map(subj => {
                        const isSelected = subj === selectedSubjectFilter;
                        return (
                          <button
                            key={subj}
                            onClick={() => setSelectedSubjectFilter(subj)}
                            className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition cursor-pointer ${
                              isSelected
                                ? 'bg-blue-600 text-white shadow-xs'
                                : 'bg-slate-200 text-slate-700 hover:bg-slate-300'
                            }`}
                          >
                            {subj}
                          </button>
                        );
                      })}
                    </div>

                    {/* Filtered Topics */}
                    {filteredTopics.length === 0 ? (
                      <div className="py-12 text-center space-y-2">
                        <div className="text-4xl">📖</div>
                        <h3 className="text-base font-bold text-slate-800">No {selectedSubjectFilter} topics</h3>
                        <p className="text-xs text-slate-500 max-w-[200px] mx-auto">
                          Tap "+ Add" above to schedule a topic for {selectedSubjectFilter}.
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-2.5">
                        {filteredTopics.map(topic => {
                          const nextRev = revisions.find(
                            r => r.topicId === topic.id && r.status === 'SCHEDULED' && r.dueAt >= virtualNow
                          );
                          return (
                            <div key={topic.id} className="bg-white border border-slate-200 rounded-xl p-3 shadow-xs">
                              <div className="font-bold text-sm text-slate-900">{topic.title}</div>
                              <div className="text-xs text-slate-500 mt-0.5">
                                {topic.chapter ? `${topic.chapter} • ` : ''}{topic.subject}
                              </div>
                              <div className="text-[11px] text-blue-600 font-medium mt-1">
                                Next due: {nextRev ? formatDateTime(nextRev.dueAt) : 'All completed'}
                              </div>
                              <div className="flex items-center justify-end space-x-2 mt-2 pt-2 border-t border-slate-100">
                                <button
                                  onClick={() => handleOpenEdit(topic)}
                                  className="text-xs font-semibold text-slate-600 hover:text-blue-600 px-2.5 py-1 border border-slate-200 rounded-md cursor-pointer"
                                >
                                  Edit
                                </button>
                                <button
                                  onClick={() => handleDeleteTopic(topic.id)}
                                  className="text-xs font-semibold text-rose-600 hover:text-rose-700 px-2.5 py-1 bg-rose-50 rounded-md cursor-pointer"
                                >
                                  Delete
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                )}

                {/* TAB 3: ALL TOPICS SCREEN */}
                {activeTab === 3 && (
                  <div className="p-4 space-y-4">
                    <div>
                      <h2 className="text-xl font-bold text-slate-900">All Topics ({topics.length})</h2>
                      <p className="text-xs text-slate-500">Complete curriculum overview</p>
                    </div>

                    {topics.length === 0 ? (
                      <div className="py-12 text-center space-y-2">
                        <div className="text-4xl">📋</div>
                        <h3 className="text-base font-bold text-slate-800">No topics created</h3>
                        <p className="text-xs text-slate-500 max-w-[200px] mx-auto">
                          All created topics across Maths, Physics, Chemistry, and English appear here.
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-2.5">
                        {topics.map(topic => {
                          const nextRev = revisions.find(
                            r => r.topicId === topic.id && r.status === 'SCHEDULED' && r.dueAt >= virtualNow
                          );
                          return (
                            <div key={topic.id} className="bg-white border border-slate-200 rounded-xl p-3 shadow-xs">
                              <div className="font-bold text-sm text-slate-900">{topic.title}</div>
                              <div className="text-xs text-slate-500 mt-0.5">
                                {topic.chapter ? `${topic.chapter} • ` : ''}{topic.subject}
                              </div>
                              <div className="text-[11px] text-blue-600 font-medium mt-1">
                                Next due: {nextRev ? formatDateTime(nextRev.dueAt) : 'All completed'}
                              </div>
                              <div className="flex items-center justify-end space-x-2 mt-2 pt-2 border-t border-slate-100">
                                <button
                                  onClick={() => handleOpenEdit(topic)}
                                  className="text-xs font-semibold text-slate-600 hover:text-blue-600 px-2.5 py-1 border border-slate-200 rounded-md cursor-pointer"
                                >
                                  Edit
                                </button>
                                <button
                                  onClick={() => handleDeleteTopic(topic.id)}
                                  className="text-xs font-semibold text-rose-600 hover:text-rose-700 px-2.5 py-1 bg-rose-50 rounded-md cursor-pointer"
                                >
                                  Delete
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Bottom Navigation Bar (4 tabs with emojis) */}
              <div className="bg-white border-t border-slate-200 grid grid-cols-4 py-1.5 px-2">
                {[
                  { label: 'Home', emoji: '🏠' },
                  { label: 'Revise', emoji: '⏰' },
                  { label: 'Subjects', emoji: '📚' },
                  { label: 'All', emoji: '📋' }
                ].map((item, idx) => {
                  const isSelected = activeTab === idx;
                  return (
                    <button
                      key={item.label}
                      onClick={() => setActiveTab(idx)}
                      className={`flex flex-col items-center justify-center py-1 transition cursor-pointer ${
                        isSelected ? 'text-blue-600 font-bold' : 'text-slate-400 font-medium'
                      }`}
                    >
                      <span className="text-lg">{item.emoji}</span>
                      <span className="text-[10px] mt-0.5">{item.label}</span>
                    </button>
                  );
                })}
              </div>

              {/* ModalBottomSheet Simulation for Add/Edit */}
              {showSheet && (
                <div className="absolute inset-0 bg-black/50 z-40 flex flex-col justify-end animate-in fade-in">
                  <div className="bg-white rounded-t-3xl p-5 max-h-[88%] overflow-y-auto space-y-4 shadow-2xl">
                    <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                      <h3 className="font-bold text-base text-slate-900">
                        {editingTopic ? 'Edit Study Topic' : 'Add Study Topic'}
                      </h3>
                      <button
                        onClick={() => setShowSheet(false)}
                        className="text-xs text-slate-400 hover:text-slate-700 p-1"
                      >
                        ✕
                      </button>
                    </div>

                    {/* Subject selection */}
                    <div>
                      <label className="text-xs font-semibold text-slate-600 block mb-1">Subject</label>
                      <div className="flex space-x-1.5">
                        {SUBJECTS.map(s => (
                          <button
                            key={s}
                            type="button"
                            onClick={() => setSheetSubject(s)}
                            className={`px-2.5 py-1 text-xs rounded-full font-medium transition cursor-pointer ${
                              sheetSubject === s
                                ? 'bg-blue-600 text-white'
                                : 'bg-slate-100 text-slate-600'
                            }`}
                          >
                            {s}
                          </button>
                        ))}
                      </div>
                    </div>

                    {/* Title */}
                    <div>
                      <label className="text-xs font-semibold text-slate-600 block mb-1">Topic Title *</label>
                      <input
                        type="text"
                        placeholder="e.g. Thermodynamics Laws"
                        value={sheetTitle}
                        onChange={e => setSheetTitle(e.target.value)}
                        className="w-full text-xs px-3 py-2 border border-slate-300 rounded-lg focus:outline-blue-500"
                      />
                    </div>

                    {/* Chapter */}
                    <div>
                      <label className="text-xs font-semibold text-slate-600 block mb-1">Chapter (optional)</label>
                      <input
                        type="text"
                        placeholder="e.g. Chapter 4: Heat & Energy"
                        value={sheetChapter}
                        onChange={e => setSheetChapter(e.target.value)}
                        className="w-full text-xs px-3 py-2 border border-slate-300 rounded-lg focus:outline-blue-500"
                      />
                    </div>

                    {/* 24-hour Revision Alert Time */}
                    <div>
                      <label className="text-xs font-semibold text-slate-600 block mb-1">
                        Daily Revision Alert Time (24h)
                      </label>
                      <div className="flex items-center space-x-3 bg-slate-50 p-2.5 rounded-xl border border-slate-200">
                        <Clock className="w-4 h-4 text-blue-600 shrink-0" />
                        <div className="flex items-center space-x-2">
                          <select
                            value={sheetHour}
                            onChange={e => setSheetHour(parseInt(e.target.value, 10))}
                            className="bg-white border border-slate-300 rounded-md px-2 py-1 text-xs font-semibold"
                          >
                            {Array.from({ length: 24 }).map((_, h) => (
                              <option key={h} value={h}>
                                {String(h).padStart(2, '0')}:00
                              </option>
                            ))}
                          </select>
                          <span className="text-xs text-slate-400 font-bold">:</span>
                          <select
                            value={sheetMinute}
                            onChange={e => setSheetMinute(parseInt(e.target.value, 10))}
                            className="bg-white border border-slate-300 rounded-md px-2 py-1 text-xs font-semibold"
                          >
                            {[0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55].map(m => (
                              <option key={m} value={m}>
                                {String(m).padStart(2, '0')}
                              </option>
                            ))}
                          </select>
                        </div>
                        <span className="text-xs text-slate-500 font-medium ml-auto">
                          Alert at -2 min
                        </span>
                      </div>
                    </div>

                    {/* Intervals */}
                    <div>
                      <label className="text-xs font-semibold text-slate-600 block mb-1">
                        Revision Intervals (days, comma-separated)
                      </label>
                      <input
                        type="text"
                        value={sheetIntervalsText}
                        onChange={e => setSheetIntervalsText(e.target.value)}
                        className="w-full text-xs px-3 py-2 border border-slate-300 rounded-lg focus:outline-blue-500 font-mono"
                      />
                      <span className="text-[10px] text-slate-400 mt-1 block">
                        Default: 3,7,14,21,30,45,60,90,120,180,365
                      </span>
                    </div>

                    {/* Live Revision Schedule Preview */}
                    <div>
                      <label className="text-xs font-semibold text-slate-600 block mb-1">
                        Scheduled Revision Previews (Live)
                      </label>
                      <div className="bg-slate-50 border border-slate-200 rounded-xl p-2.5 text-xs text-slate-600 space-y-1 max-h-32 overflow-y-auto">
                        {previewList.length === 0 ? (
                          <div className="text-rose-500 text-[11px]">Enter valid interval numbers</div>
                        ) : (
                          <>
                            {previewList.slice(0, 6).map((ts, idx) => (
                              <div key={idx} className="flex items-center justify-between text-[11px]">
                                <span>• Rev {idx + 1} (+{parsedSheetIntervals[idx]}d)</span>
                                <span className="font-mono text-slate-800">{formatDateTime(ts)}</span>
                              </div>
                            ))}
                            {previewList.length > 6 && (
                              <div className="text-[11px] font-medium text-blue-600 pt-1 text-center">
                                ...and {previewList.length - 6} more scheduled revisions
                              </div>
                            )}
                          </>
                        )}
                      </div>
                    </div>

                    {/* Save Pill Button */}
                    <button
                      id="btn-save-topic"
                      disabled={!sheetTitle.trim() || parsedSheetIntervals.length === 0}
                      onClick={handleSaveTopic}
                      className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-40 text-white font-bold text-xs py-3 rounded-full transition shadow-md cursor-pointer"
                    >
                      {editingTopic ? 'Update Topic' : 'Save Topic'}
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Side Controls / Database & Time Travel Simulator */}
            <div className="w-full max-w-sm space-y-4">
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center space-x-2">
                    <Clock className="w-4 h-4 text-blue-400" />
                    <h3 className="font-bold text-sm text-slate-200">Virtual Time Travel</h3>
                  </div>
                  <button
                    onClick={() => setVirtualNow(Date.now())}
                    className="text-[11px] text-blue-400 hover:underline flex items-center space-x-1"
                  >
                    <RefreshCw className="w-3 h-3" />
                    <span>Real Time</span>
                  </button>
                </div>
                <p className="text-xs text-slate-400 mb-3 leading-relaxed">
                  Test the static scheduler and the hard product rules. Advance virtual time to see scheduled revisions become <span className="text-emerald-400 font-semibold">Due Today</span> or <span className="text-rose-400 font-semibold">Missed</span>!
                </p>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    onClick={() => setVirtualNow(prev => prev + 24 * 3600 * 1000)}
                    className="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs py-2 px-3 rounded-xl border border-slate-700 font-medium transition cursor-pointer"
                  >
                    +1 Day
                  </button>
                  <button
                    onClick={() => setVirtualNow(prev => prev + 3 * 24 * 3600 * 1000)}
                    className="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs py-2 px-3 rounded-xl border border-slate-700 font-medium transition cursor-pointer"
                  >
                    +3 Days (1st rev)
                  </button>
                  <button
                    onClick={() => setVirtualNow(prev => prev + 7 * 24 * 3600 * 1000)}
                    className="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs py-2 px-3 rounded-xl border border-slate-700 font-medium transition cursor-pointer"
                  >
                    +7 Days
                  </button>
                  <button
                    onClick={() => setVirtualNow(prev => prev + 30 * 24 * 3600 * 1000)}
                    className="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs py-2 px-3 rounded-xl border border-slate-700 font-medium transition cursor-pointer"
                  >
                    +30 Days
                  </button>
                </div>
                <div className="mt-3 pt-2.5 border-t border-slate-800 text-[11px] text-slate-400 font-mono">
                  Virtual Now: {formatDateTime(virtualNow)}
                </div>
              </div>

              {/* Hard Product Rules Card */}
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-2.5">
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  <h3 className="font-bold text-sm text-slate-200">Hard Product Rules Verified</h3>
                </div>
                <ul className="text-xs text-slate-400 space-y-1.5 list-disc pl-4">
                  <li><strong className="text-slate-200">Fresh install:</strong> 0 topics, 0 revisions (no demo data).</li>
                  <li><strong className="text-slate-200">4 Fixed Subjects:</strong> Maths, Physics, Chemistry, English.</li>
                  <li><strong className="text-slate-200">Static intervals:</strong> Revisions anchored to base timestamp.</li>
                  <li><strong className="text-slate-200">Edit rule:</strong> Keep completed history, regenerate SCHEDULED.</li>
                  <li><strong className="text-slate-200">Missed rule:</strong> SCHEDULED with dueAt &lt; now stays missed until marked done.</li>
                </ul>
                <div className="pt-2 border-t border-slate-800 flex items-center justify-between">
                  <button
                    onClick={handleResetDatabase}
                    className="text-xs text-rose-400 hover:text-rose-300 flex items-center space-x-1 cursor-pointer"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    <span>Reset Room DB (etea_blank_v1)</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB: PROJECT FILES BROWSER */}
        {appMode === 'files' && (
          <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
            {/* Sidebar File List */}
            <div className="w-full md:w-80 border-r border-slate-800 bg-slate-900/50 flex flex-col">
              <div className="p-3 border-b border-slate-800">
                <input
                  type="text"
                  placeholder="Filter 23 files..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-1.5 text-xs text-slate-200 focus:outline-blue-500 placeholder-slate-500"
                />
              </div>
              <div className="flex-1 overflow-y-auto p-2 space-y-1">
                {filteredProjectFiles.map(file => {
                  const isSelected = file.path === selectedFilePath;
                  return (
                    <button
                      key={file.path}
                      onClick={() => setSelectedFilePath(file.path)}
                      className={`w-full text-left px-2.5 py-2 rounded-lg text-xs font-mono transition flex items-center justify-between cursor-pointer ${
                        isSelected
                          ? 'bg-blue-600/20 text-blue-300 border border-blue-500/40'
                          : 'text-slate-400 hover:bg-slate-800/60 hover:text-slate-200'
                      }`}
                    >
                      <div className="flex items-center space-x-2 truncate">
                        <FileCode className="w-3.5 h-3.5 shrink-0" />
                        <span className="truncate">{file.path}</span>
                      </div>
                      <span className="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 shrink-0 ml-1">
                        {file.category}
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* File Viewer */}
            <div className="flex-1 flex flex-col bg-slate-950 overflow-hidden">
              <div className="p-3 border-b border-slate-800 flex items-center justify-between bg-slate-900/40">
                <div>
                  <div className="font-mono text-xs text-blue-400 font-semibold">{selectedFile.path}</div>
                  <div className="text-[11px] text-slate-400">{selectedFile.description}</div>
                </div>
                <button
                  onClick={() => handleCopyCode(selectedFile.content, selectedFile.path)}
                  className="flex items-center space-x-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 px-3 py-1.5 rounded-lg text-xs font-medium border border-slate-700 transition cursor-pointer"
                >
                  {copiedFile === selectedFile.path ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                      <span className="text-emerald-400">Copied!</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      <span>Copy File</span>
                    </>
                  )}
                </button>
              </div>
              <pre className="flex-1 p-4 overflow-auto font-mono text-xs text-slate-300 leading-relaxed bg-slate-950 select-text">
                <code>{selectedFile.content}</code>
              </pre>
            </div>
          </div>
        )}

        {/* TAB: GITHUB ACTIONS SETUP GUIDE */}
        {appMode === 'guide' && (
          <div className="flex-1 p-6 overflow-y-auto max-w-4xl mx-auto space-y-6">
            <div>
              <h2 className="text-2xl font-bold text-slate-100">GitHub Actions CI/CD & Test Checklist</h2>
              <p className="text-sm text-slate-400 mt-1">
                How to push files, trigger the official Gradle 8.4 workflow, download the debug APK, and test all requirements.
              </p>
            </div>

            {/* Quick Flow Steps */}
            <div className="grid md:grid-cols-3 gap-4">
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-2">
                <div className="w-7 h-7 rounded-lg bg-blue-500/20 text-blue-400 font-bold flex items-center justify-center text-sm">
                  1
                </div>
                <h4 className="font-semibold text-slate-200 text-sm">Create Repository</h4>
                <p className="text-xs text-slate-400 leading-relaxed">
                  Create a repo named <code className="text-blue-300 bg-slate-800 px-1 py-0.5 rounded">etea-study-plan</code>. Open github.dev on phone browser or desktop.
                </p>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-2">
                <div className="w-7 h-7 rounded-lg bg-purple-500/20 text-purple-400 font-bold flex items-center justify-center text-sm">
                  2
                </div>
                <h4 className="font-semibold text-slate-200 text-sm">Add the 23 Files</h4>
                <p className="text-xs text-slate-400 leading-relaxed">
                  Paste each file from the Files tab or upload the downloaded ZIP. Commit directly to branch <code className="text-purple-300 bg-slate-800 px-1 py-0.5 rounded">main</code>.
                </p>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 space-y-2">
                <div className="w-7 h-7 rounded-lg bg-emerald-500/20 text-emerald-400 font-bold flex items-center justify-center text-sm">
                  3
                </div>
                <h4 className="font-semibold text-slate-200 text-sm">Download APK</h4>
                <p className="text-xs text-slate-400 leading-relaxed">
                  GitHub Actions will automatically run <code className="text-emerald-300 bg-slate-800 px-1 py-0.5 rounded">./gradlew assembleDebug</code> and attach <code className="text-emerald-300">app-debug.apk</code> as an artifact.
                </p>
              </div>
            </div>

            {/* Testing Checklist */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
              <h3 className="text-base font-bold text-slate-200 flex items-center space-x-2">
                <CheckCircle2 className="w-5 h-5 text-blue-400" />
                <span>Manual Verification Checklist</span>
              </h3>
              <div className="space-y-2.5 text-xs text-slate-300">
                <div className="flex items-start space-x-2.5 bg-slate-950 p-3 rounded-xl border border-slate-800/80">
                  <span className="text-blue-400 font-bold shrink-0">Step 1:</span>
                  <span><strong>Fresh Install:</strong> Open app. Verify Home tab shows Total 0 topics, 0 Today, 0 Missed. Verify no demo data exists.</span>
                </div>
                <div className="flex items-start space-x-2.5 bg-slate-950 p-3 rounded-xl border border-slate-800/80">
                  <span className="text-blue-400 font-bold shrink-0">Step 2:</span>
                  <span><strong>Add Physics Topic:</strong> Select Physics, title "Gravitation", chapter "Chapter 6", time 18:30. Verify live preview formats exact future timestamps (+3d, +7d, etc.).</span>
                </div>
                <div className="flex items-start space-x-2.5 bg-slate-950 p-3 rounded-xl border border-slate-800/80">
                  <span className="text-blue-400 font-bold shrink-0">Step 3:</span>
                  <span><strong>Edit Topic:</strong> Edit intervals or time. Verify completed revisions remain intact and only SCHEDULED revisions regenerate from original base date.</span>
                </div>
                <div className="flex items-start space-x-2.5 bg-slate-950 p-3 rounded-xl border border-slate-800/80">
                  <span className="text-blue-400 font-bold shrink-0">Step 4:</span>
                  <span><strong>Delete Topic:</strong> Delete the topic. Verify all associated revisions are removed and count returns to 0.</span>
                </div>
                <div className="flex items-start space-x-2.5 bg-slate-950 p-3 rounded-xl border border-slate-800/80">
                  <span className="text-blue-400 font-bold shrink-0">Step 5:</span>
                  <span><strong>Persistence:</strong> Kill and relaunch app. Room database <code className="text-blue-400 font-mono">etea_blank_v1</code> persists all data.</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
