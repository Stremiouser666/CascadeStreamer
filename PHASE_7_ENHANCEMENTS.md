# CascadeStreamer Phase 7 - Enhancements & Implementation Guide

## Overview
Phase 7 focuses on series/show metadata integration, artwork display, local file support, and UI polish for Android TV streaming.

---

## COMPLETED FEATURES ✅

### 1. TVMazeManager with Live API Calls
- **Status:** ✅ WORKING
- **Features:**
  - Search shows by query: `searchShows(query: String)`
  - Get show details: `getShowDetails(showId: Int)`
  - Get episodes: `getShowEpisodes(showId: Int)`
  - Includes: name, rating, genres, summary, image URLs (medium/original)
- **File:** `TVMazeManager.kt`, `TVMazeService.kt`
- **Retrofit dependency:** Configured and working

### 2. FanArtManager with Live API Calls
- **Status:** ✅ WORKING
- **Features:**
  - Get artwork for show: `getArtwork(tvdbId: Int)`
  - Get highest-rated poster: `getShowPoster(tvdbId: Int)`
  - Get highest-rated backdrop: `getShowBackdrop(tvdbId: Int)`
  - Supports: posters, backdrops, episode thumbnails, logos
- **File:** `FanArtManager.kt`, `FanArtService.kt`

### 3. Local File Play
- **Status:** ✅ WORKING
- **Features:**
  - System file picker (Intent.ACTION_OPEN_DOCUMENT)
  - Supports all video formats (mp4, mkv, webm, avi, etc.)
  - Handles both file:// and content:// URIs
  - Plays local files directly in VideoPlayer
- **File:** `FileBrowserScreen.kt`

### 4. Save Local Files to Watchlist
- **Status:** ✅ WORKING
- **Features:**
  - Local files auto-added to recent videos on selection
  - Persists to disk via LocalStorageManager
  - Shows in "Recent Videos" on HomeScreen
- **Implementation:** `AppState.addVideo()` called on file selection

### 5. SeriesDetailScreen UI
- **Status:** ✅ WORKING (needs enhancement)
- **Features:**
  - Hero backdrop with title overlay
  - Play button + action buttons
  - Show metadata: rating, year, genres
  - Season selector dropdown
  - Episodes section with thumbnails
  - Cast & Crew section (placeholder)
- **File:** `SeriesDetailScreen.kt`
- **Current:** Shows Season 1 only

### 6. SearchSeriesScreen
- **Status:** ✅ WORKING
- **Features:**
  - Search TVMaze by show name
  - Displays search results with show info
  - Click to view full SeriesDetailScreen
- **File:** `SearchSeriesScreen.kt`
- **Access:** "🔍 Search Series" button on HomeScreen

### 7. Retrofit Dependencies
- **Status:** ✅ ADDED
- **Packages:**
  - `com.squareup.retrofit2:retrofit:2.9.0`
  - `com.squareup.retrofit2:converter-gson:2.9.0`
  - `com.squareup.okhttp3:okhttp:4.11.0`
  - `io.coil-kt:coil-compose:2.4.0` (image loading)

### 8. Runtime Permissions
- **Status:** ✅ WORKING
- **Permissions:**
  - READ_EXTERNAL_STORAGE (for local file access)
  - Requested at runtime when "Open File" clicked
  - Uses ActivityResultContracts.RequestPermission()

---

## IN PROGRESS / NEEDS WORK 🔧

### SeriesDetailScreen Enhancements
- **Issue:** Only shows Season 1 episodes
- **Fix needed:** Load ALL seasons from TVMaze, implement season selector
- **Current:** Episodes hardcoded to Season 1
- **TODO:**
  1. Modify SeriesDetailScreen to show all seasons
  2. Create dropdown/selector for season choice
  3. Load episodes for selected season dynamically
  4. Show episode count per season

### Image Loading Strategy (Memory-Only)
- **Status:** Implemented for SeriesDetailScreen
- **Current:** Coil loads images from URL (no disk cache)
- **Behavior:** Images cached in memory, cleared on app close
- **Note:** Auto-cleanup when show removed from watchlist

---

## PHASE 7 FEATURES - PLANNED 📋

### A. SERIES DETAIL SCREEN UI IMPROVEMENTS

#### 1. Load All Seasons (HIGH PRIORITY)
- Currently: Only Season 1 visible
- Needed: Fetch all seasons from TVMaze API
- Implementation: Modify SeriesDetailScreen to:
  - Add season dropdown selector
  - Load episodes dynamically per season
  - Update UI when season changes

#### 2. Description Popup (Medium Priority)
- Current: Description shown inline, text cut off
- Better: Clickable description → scrollable popup
- Features:
  - Full description text scrollable
  - Font size controls (+/- buttons)
  - Close button or back gesture

#### 3. Cast Info Display (Medium Priority)
- Current: "Cast info coming soon" placeholder
- TODO:
  - Fetch cast from TVMaze API (person name, character, image)
  - Display as horizontal scrollable list
  - Show headshots with names/character roles

### B. LOCAL FILE FEATURES

#### 1. Remove from Watchlist (UI Polish)
- **Feature:** Long-press video in recent to remove with confirmation
- **Implementation:**
  - Add long-press handler to VideoItem in HomeScreen
  - Show delete confirmation dialog
  - Remove from appState.videos and persist
- **Priority:** Later (non-critical)

#### 2. Folder Playback
- **Feature:** Select folder → play all videos sequentially
- **Implementation:**
  - Add folder selection to file browser
  - Auto-create playlist from folder contents
  - Play through folder sequentially with next/previous
  - Track position in folder
- **Priority:** Phase 7 feature list
- **Use case:** Test with multiple local episodes

#### 3. File Picker Research (Lower Priority)
- **Reference:** mpv-android has both legacy and modern implementations
  - MPVFilePickerFragment.java (legacy manual browsing)
  - MPVDocumentPickerFragment.java (modern intent-based)
  - FilePickerActivity.kt (orchestrator)
- **When needed:** After core file features working
- **Option:** Can improve custom file browser OR adopt their pattern

### C. AUTO-LOOKUP SERIES INFO (UX Improvement)

#### Current Flow
- User manually searches via "🔍 Search Series" button

#### Future Flow
- Auto-lookup series when video added (if TV show format detected)
- Detect show name from video filename (e.g., "Show.S01E01.Title.mp4")
- Auto-search TVMaze in background
- If found, show "ℹ️ Series Info" button (instead of generic info)
- User can tap to view full SeriesDetailScreen
- In Settings: Choose info source (TVMaze, FanArt, Manual)

**Why:** Seamless experience - series info appears automatically for TV shows

### D. IMAGE LOADING & CACHING

#### Current Strategy
- **Memory-only caching:** No disk storage
- **Fresh each session:** Images reload on app start
- **Auto-cleanup:** Clears on app close or show removal
- **Why:** Keeps app lightweight, respects user storage

#### Coil Configuration
- Using `coil-compose:2.4.0`
- AsyncImage composable handles memory caching
- No disk cache layer configured (intentional)

---

## FILE STRUCTURE - NEW PHASE 7 FILES

```
app/src/main/java/com/cascadestreamer/app/
├── managers/
│   ├── TVMazeManager.kt (DONE)
│   ├── TVMazeService.kt (DONE)
│   ├── FanArtManager.kt (DONE)
│   └── FanArtService.kt (DONE)
├── ui/
│   ├── SeriesDetailScreen.kt (DONE - needs enhancement)
│   ├── SearchSeriesScreen.kt (DONE)
│   ├── FileBrowserScreen.kt (DONE - system picker)
│   └── ScrollableScreen.kt (DONE - wrapper for all screens)
└── states/
    └── AppState.kt (updated with addVideo method)
```

---

## TESTING CHECKLIST

### Phase 7 Features to Test
- [ ] Search series by name (TVMaze)
- [ ] Select series → view SeriesDetailScreen
- [ ] Play button on SeriesDetailScreen works
- [ ] Season selector shows all seasons
- [ ] Episodes load for selected season
- [ ] Open local file → plays correctly
- [ ] Local file appears in recent videos
- [ ] Navigate between screens without crashing
- [ ] Rotation doesn't lose state
- [ ] App closes cleanly

---

## KNOWN ISSUES & WORKAROUNDS

### 1. SeriesDetailScreen Episodes
- **Issue:** Only Season 1 episodes display
- **Cause:** Hardcoded to first season in UI
- **Workaround:** None - needs UI update
- **Fix:** Load all seasons, add season dropdown

### 2. File Browser Path Issues (RESOLVED)
- **Was:** `/storage/emulated/0` returned CanRead: false
- **Solution:** Switched to Intent.ACTION_OPEN_DOCUMENT (system picker)
- **Result:** Works perfectly now

### 3. Sed Complexity (RESOLVED)
- **Issue:** Complex sed commands broke scope-dependent files
- **Decision:** Switched to full file rewrites with `cat >`
- **Lesson:** Simple tools > complex tools for file manipulation

---

## NEXT SESSION PRIORITIES

1. **Load ALL seasons** in SeriesDetailScreen (HIGH PRIORITY)
   - Fetch seasons from TVMaze
   - Implement season selector
   - Load episodes dynamically

2. **Test** SearchSeriesScreen + SeriesDetailScreen workflow
   - Search for show
   - View full series info
   - Select episodes

3. **Add cast info** to SeriesDetailScreen (if time)
   - Fetch cast from TVMaze API
   - Display as scrollable list

4. **Description popup** (UI polish)
   - Make description clickable/expandable
   - Add font size controls

---

## DEVELOPMENT NOTES

### Code Quality Lessons
- **Avoid sed on:** App.kt, AppState.kt, complex files with scope dependencies
- **Use sed for:** Simple string replacements, imports, simple configs
- **Better approach:** Full file rewrites for complex changes (safer, clearer)

### Permission Handling
- Runtime permissions required on Android 6+
- Use `ActivityResultContracts.RequestPermission()`
- Check `ContextCompat.checkSelfPermission()` before accessing storage

### Compose Best Practices
- Use `LaunchedEffect` for side effects that should run when state changes
- Use `rememberCoroutineScope()` for launching async operations
- State should be owned by parent, passed down to children

### API Design
- Keep managers simple: one responsibility per manager
- Retrofit services for API calls, managers for business logic
- Return data classes from managers, not raw API responses
- Use suspend functions for async operations

---

## RESOURCES & REFERENCES

### APIs
- **TVMaze API:** https://www.tvmaze.com/api
- **FanArt.tv API:** https://webservice.fanart.tv/v3/
- **Retrofit Documentation:** https://square.github.io/retrofit/

### Libraries
- **Retrofit 2.9.0** - HTTP client
- **Coil 2.4.0** - Image loading
- **OkHttp 4.11.0** - HTTP layer

### Previous Phase Handovers
- `TECHNICAL_HANDOVER.md` - Full project state, architecture
- `TECHNICAL_HANDOVER_ENHANCED.md` - User profile, interaction style, sed lessons
- `CascadeStreamer-raw-links.txt` - Raw GitHub URLs for source files

---

## SUMMARY

Phase 7 is well underway with:
- ✅ TVMaze & FanArt APIs integrated and working
- ✅ SeriesDetailScreen displaying with metadata
- ✅ SearchSeriesScreen finding shows
- ✅ Local file play fully functional
- ✅ Files saved to watchlist

**Next focus:** Load all seasons dynamically, enhance UI with descriptions & cast info.

**Timeline:** Functional by end of Phase 7 prep, polish in final sessions.

---

**Last Updated:** March 8, 2026
**Status:** Active Development
**Next Review:** After season loading implementation
