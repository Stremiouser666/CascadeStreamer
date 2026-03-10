# CascadeStreamer Phase 7 - Enhancements & Implementation Guide

## Overview
Phase 7 focuses on series/show metadata integration, artwork display, local file support, expandable UI, and code organization for Android TV streaming.

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

### 5. SeriesDetailScreen UI with Dynamic Seasons
- **Status:** ✅ WORKING
- **Features:**
  - Hero backdrop with title overlay
  - Play button + action buttons
  - Show metadata: rating, year, genres
  - **NEW:** Season dropdown selector - shows ALL seasons
  - **NEW:** Episodes load dynamically per season selected
  - **NEW:** Scrollable episodes list
  - Cast & Crew section (placeholder)
- **File:** `SeriesDetailScreen.kt`
- **Latest:** Loads ALL seasons from TVMaze, no hardcoding

### 6. Description Popup with Font Size Controls
- **Status:** ✅ WORKING
- **Features:**
  - Show description clickable → opens popup
  - Full scrollable description text
  - Font size controls: "Smaller" / "Larger" buttons
  - Shows current font size (10-24)
  - Close button to dismiss
- **File:** `DescriptionPopup.kt`
- **Access:** Tap "Tap to expand ↕️" under show summary
- **Text labels:** Using "Smaller"/"Larger" instead of symbols for reliability

### 7. SearchSeriesScreen
- **Status:** ✅ WORKING
- **Features:**
  - Search TVMaze by show name
  - Displays search results with show info
  - Click to view full SeriesDetailScreen with all seasons/episodes
- **File:** `SearchSeriesScreen.kt`
- **Access:** "🔍 Search Series" button on HomeScreen

### 8. Retrofit & Image Loading Dependencies
- **Status:** ✅ ADDED
- **Packages:**
  - `com.squareup.retrofit2:retrofit:2.9.0`
  - `com.squareup.retrofit2:converter-gson:2.9.0`
  - `com.squareup.okhttp3:okhttp:4.11.0`
  - `io.coil-kt:coil-compose:2.4.0` (image loading)

### 9. Runtime Permissions
- **Status:** ✅ WORKING
- **Permissions:**
  - READ_EXTERNAL_STORAGE (for local file access)
  - Requested at runtime when "Open File" clicked
  - Uses ActivityResultContracts.RequestPermission()

---

## IN PROGRESS / NEEDS WORK 🔧

### Episode Description Popups
- **Issue:** Episodes can be selected but summaries not expandable like show description
- **TODO:**
  1. Make episode summaries clickable
  2. Open same DescriptionPopup with font size controls
  3. Apply to SeriesDetailScreen episodes

---

## CODE REFACTORING - FOLDER STRUCTURE 📁

### Why Refactor?
- SeriesDetailScreen growing large (200+ lines)
- VideoPlayerScreen will be similarly large
- HomeScreen and SettingsScreen need organization
- Easier maintenance and future additions

### Phase 7 Refactoring Plan

#### 1. SeriesDetailScreen Refactor (READY)
**Current:** Monolithic file with mixed concerns
**Target:** Feature folder structure

```
ui/seriesdetail/
├── SeriesDetailScreen.kt (main screen logic)
├── EpisodeCard.kt (individual episode display)
├── SeasonSelector.kt (season dropdown logic)
└── CastSection.kt (cast display - future)
```

**Benefits:**
- Each component in own file
- Easy to add cast section later
- Clean imports and readability
- Reusable components

#### 2. HomeScreen Refactor (NEXT)
**Current:** Single file with video grid, buttons, playlists
**Target:** Organized folder

```
ui/home/
├── HomeScreen.kt (main screen)
├── VideoItem.kt (video card component)
├── PlaylistGrid.kt (playlist section)
└── QuickActionButtons.kt (Open File, Search Series buttons)
```

#### 3. SettingsScreen Refactor (NEXT)
**Current:** Settings in single file
**Target:** Organized folder

```
ui/settings/
├── SettingsScreen.kt (main screen)
├── QualitySelector.kt (quality selection option)
├── AppInfoSection.kt (app version, credits)
└── PreferenceItem.kt (reusable preference row)
```

#### 4. VideoPlayerScreen Refactor (FUTURE)
**Current:** Will become large with playback controls
**Target:** Feature folder

```
ui/videoplayer/
├── VideoPlayerScreen.kt (main player)
├── PlaybackControls.kt (play/pause/seek)
├── QualitySelector.kt (in-player quality picker)
└── SubtitlePanel.kt (subtitle controls - future)
```

---

## PHASE 7 FEATURES - REMAINING 📋

### A. SERIES DETAIL IMPROVEMENTS

#### 1. Episode Description Popups (HIGH PRIORITY)
- Make episode summaries clickable
- Open DescriptionPopup with same font size controls
- Applies to both show and episodes

#### 2. Cast Info Display (MEDIUM PRIORITY)
- Fetch cast from TVMaze API (person name, character, image)
- Display as horizontal scrollable list
- Show headshots with names/character roles

### B. LOCAL FILE FEATURES

#### 1. Remove from Watchlist (UI POLISH)
- **Feature:** Long-press video in recent to remove with confirmation
- **Implementation:**
  - Add long-press handler to VideoItem
  - Show delete confirmation dialog
  - Remove from appState.videos and persist
- **Priority:** Phase 7 polish

#### 2. Folder Playback (PHASE 7 FEATURE)
- **Feature:** Select folder → play all videos sequentially
- **Implementation:**
  - Add folder selection to file browser
  - Auto-create playlist from folder contents
  - Play through sequentially with next/previous
  - Track position in folder
- **Use case:** Test with multiple local episodes

### C. AUTO-LOOKUP SERIES INFO (UX IMPROVEMENT)

#### Current Flow
- User manually searches via "🔍 Search Series" button

#### Future Flow
- Auto-lookup series when video added (if TV show format detected)
- Detect show name from video filename (e.g., "Show.S01E01.Title.mp4")
- Auto-search TVMaze in background
- If found, show "ℹ️ Series Info" button (instead of generic info)
- User can tap to view full SeriesDetailScreen
- In Settings: Choose info source (TVMaze, FanArt, Manual)

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

## FILE STRUCTURE - CURRENT PHASE 7 FILES

```
app/src/main/java/com/cascadestreamer/app/
├── managers/
│   ├── TVMazeManager.kt ✅
│   ├── TVMazeService.kt ✅
│   ├── FanArtManager.kt ✅
│   └── FanArtService.kt ✅
├── ui/
│   ├── SeriesDetailScreen.kt ✅ (READY FOR REFACTOR)
│   ├── DescriptionPopup.kt ✅
│   ├── SearchSeriesScreen.kt ✅
│   ├── FileBrowserScreen.kt ✅
│   ├── HomeScreen.kt (READY FOR REFACTOR)
│   ├── SettingsScreen.kt (READY FOR REFACTOR)
│   └── ScrollableScreen.kt ✅
└── states/
    └── AppState.kt ✅ (with addVideo method)
```

---

## TESTING CHECKLIST

### Phase 7 Features to Test
- [x] Search series by name (TVMaze)
- [x] Select series → view SeriesDetailScreen
- [x] Play button on SeriesDetailScreen works
- [x] Season selector shows all seasons
- [x] Episodes load for selected season
- [x] Description popup opens and scrolls
- [x] Font size controls work (Smaller/Larger)
- [x] Open local file → plays correctly
- [x] Local file appears in recent videos
- [ ] Navigate between screens without crashing
- [ ] Rotation doesn't lose state
- [ ] App closes cleanly

---

## KNOWN ISSUES & WORKAROUNDS

### 1. File Browser Path Issues (RESOLVED)
- **Was:** `/storage/emulated/0` returned CanRead: false
- **Solution:** Switched to Intent.ACTION_OPEN_DOCUMENT (system picker)
- **Result:** Works perfectly now

### 2. Sed Complexity (RESOLVED)
- **Issue:** Complex sed commands broke scope-dependent files
- **Decision:** Switched to full file rewrites with `cat >`
- **Lesson:** Simple tools > complex tools for file manipulation

### 3. Character Rendering in UI (RESOLVED)
- **Was:** − and + symbols not rendering on buttons
- **Solution:** Changed to text labels "Smaller"/"Larger"
- **Result:** Clear, readable, reliable

---

## NEXT SESSION PRIORITIES

1. **Refactor SeriesDetailScreen into folder** (HIGH PRIORITY)
   - Extract EpisodeCard.kt
   - Extract SeasonSelector.kt
   - Keep main screen clean

2. **Add episode description popups** (HIGH PRIORITY)
   - Make summaries clickable
   - Reuse DescriptionPopup component

3. **Refactor HomeScreen** (MEDIUM PRIORITY)
   - Extract VideoItem.kt
   - Extract QuickActionButtons.kt
   - Clean up main screen

4. **Test** refactored screens
   - Ensure functionality unchanged
   - Verify navigation still works

---

## GITHUB QUOTA STATUS

**Current:** ~100MB used of 500MB monthly limit
**Rate:** ~100MB per 2 weeks at current build frequency

**To Preserve:**
1. Test locally on TV BEFORE pushing
2. Bundle 3-4 changes before building
3. Only push when ready for GitHub Actions build
4. Run cleanup-artifacts.yml regularly

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
- Extract reusable components into separate files/folders

### API Design
- Keep managers simple: one responsibility per manager
- Retrofit services for API calls, managers for business logic
- Return data classes from managers, not raw API responses
- Use suspend functions for async operations

### Dialog/Popup Best Practices
- Use `Dialog()` composable for overlays
- Provide dismiss callbacks
- Clean up state on dismiss
- Make text selectable if content-heavy

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

Phase 7 is progressing excellently with:
- ✅ TVMaze & FanArt APIs integrated and working
- ✅ SeriesDetailScreen displaying with all seasons loading dynamically
- ✅ SearchSeriesScreen finding shows
- ✅ Description popup with font size controls
- ✅ Local file play fully functional
- ✅ Files saved to watchlist

**Next focus:** Refactor into folder structure, add episode popups, test thoroughly.

**Timeline:** Core features complete. Refactoring and polish this week.

---

**Last Updated:** March 9, 2026
**Status:** Active Development - Phase 7 Features Complete, Refactoring Ready
**Next Review:** After folder structure refactoring
