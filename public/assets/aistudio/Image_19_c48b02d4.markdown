---
name: Obsidian Kinetic
colors:
  surface: '#101412'
  surface-dim: '#101412'
  surface-bright: '#363a38'
  surface-container-lowest: '#0b0f0d'
  surface-container-low: '#181c1a'
  surface-container: '#1c201e'
  surface-container-high: '#272b28'
  surface-container-highest: '#323633'
  on-surface: '#e0e3df'
  on-surface-variant: '#bbcabf'
  inverse-surface: '#e0e3df'
  inverse-on-surface: '#2d312f'
  outline: '#86948a'
  outline-variant: '#3c4a42'
  surface-tint: '#4edea3'
  primary: '#4edea3'
  on-primary: '#003824'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#006c49'
  secondary: '#45dfa4'
  on-secondary: '#003825'
  secondary-container: '#00bd85'
  on-secondary-container: '#00452e'
  tertiary: '#68dba9'
  on-tertiary: '#003825'
  tertiary-container: '#3eb686'
  on-tertiary-container: '#00422c'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#68fcbf'
  secondary-fixed-dim: '#45dfa4'
  on-secondary-fixed: '#002114'
  on-secondary-fixed-variant: '#005137'
  tertiary-fixed: '#85f8c4'
  tertiary-fixed-dim: '#68dba9'
  on-tertiary-fixed: '#002114'
  on-tertiary-fixed-variant: '#005137'
  background: '#101412'
  on-background: '#e0e3df'
  surface-variant: '#323633'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 12px
  metric-display:
    fontFamily: Plus Jakarta Sans
    fontSize: 48px
    fontWeight: '800'
    lineHeight: 52px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1.25rem
  margin-tablet: 2rem
  space-2xs: 0.25rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
---

## Brand & Style

This design system expresses disciplined momentum, refined focus, and calm mastery. Tailored for high-performing individuals building lasting daily routines, the interface rejects chaotic gamification in favor of an atmospheric, premium productivity environment.

The aesthetic fuses **Tactile Minimalism** with subtle **Bioluminescent Accents**. Deep obsidian grounds the interface, preventing visual fatigue during late-night or early-morning check-ins. Surfaces feel physical and deliberate—like precision-milled slate—while radiant emerald and mint accents serve as kinetic rewards for user consistency and micro-achievements. Every interaction prioritizes deliberate focus, physical weight, and crisp readability.

## Colors

The palette relies on a tonal hierarchy of dark obsidian grounds, charcoal-tinted surfaces, muted botanical boundary lines, and vibrant emerald energy tokens.

- **Background & Canvas:**
  - Base Obsidian: `#0B0F0D` (primary canvas, deep ambient black with subtle pine undertone).
  - Raised Obsidian: `#101512` (subtle level-0 alternate canvas and secondary backgrounds).
- **Surface & Containers:**
  - Surface Default: `#171F1A` (standard cards, habit rows, and bottom sheets).
  - Surface Raised: `#1C2420` (hover states, selected tiles, active states).
  - Surface Elevated: `#222C27` (floating modals, popovers, nested container elements).
- **Borders & Dividers:**
  - Structural Stroke: `#28352F` (subtle muted green-gray hairline boundaries).
  - Subtle Stroke: `#1E2823` (internal card dividers, progress rail backgrounds).
- **Accents & Momentum:**
  - Emerald Core (Primary): `#10B981` (primary action triggers, completed streaks, active states).
  - Mint Glow (Secondary / Highlight): `#34D399` (interactive highlights, streak milestone badges, metrics).
  - Deep Emerald (Tertiary): `#059669` (pressed states, surface accents, dense fills).
- **Typography & Content:**
  - Primary Text: `#F1F5F9` (high-contrast headings and active metrics).
  - Secondary Text: `#E2E8F0` (body copy, card titles, standard inputs).
  - Muted Text: `#94A3B8` (helper copy, timestamps, inactive dates, unit labels).
  - Disabled Text: `#475569` (inactive controls, unavailable states).

## Typography

Plus Jakarta Sans governs the entire typographic structure. Its geometric baseline paired with humanist apertures creates high legibility on dark surfaces while conveying modern confidence.

- **Numbers & Metrics:** Use `metric-display` with tabular figure settings (`tnum`) for timers, day streaks, and habit completion stats to prevent layout shifting.
- **Letter Spacing:** Apply -0.02em tracking for headlines above 24px to retain visual density. Apply +0.02em tracking on `label-sm` when styled in all-caps.
- **Contrast Pairing:** Always pair primary white/slate titles (`#F1F5F9`) directly above muted slate descriptions (`#94A3B8`) to construct effortless scannability across dense daily schedules.

## Layout & Spacing

The layout operates on a mobile-first 4-column structure expanding to 8 columns on tablet viewports, built on an uncompromising 4px/8px modular spacing scale.

- **Mobile Viewports (<600px):** 4 columns, fixed margin of `1.25rem` (20px), gutter of `0.75rem` (12px). Habit cards and timeline lists stretch fluidly across all 4 columns. Bottom navigation and sticky tracking bars anchor to bottom safe areas.
- **Tablet / Large Handhelds (600px–1024px):** 8 columns, margin of `2rem` (32px), gutter of `1rem` (16px). Cards reflow into a dual-column layout (e.g., Active Habits on the left, Streak Visualizer and Metrics on the right).
- **Component Insets:** Compact components (chips, streak pills) utilize `space-2xs` (4px) vertical and `space-xs` (8px) horizontal padding. Cards use unified `space-md` (16px) or `space-lg` (24px) internal padding.

## Elevation & Depth

This design system avoids heavy drop shadows, relying instead on **Tonal Stacking** augmented by **Micro-Borders** and **Subtle Bioluminescent Ambient Glows**.

- **Level 0 (Canvas Base):** Ground color `#0B0F0D`. Flat, zero elevation.
- **Level 1 (Habit Cards, List Items):** Background `#171F1A`, encapsulated by a 1px solid stroke in `#28352F`. Shadow is entirely absent, relying solely on surface contrast against the base canvas.
- **Level 2 (Interactive Flyouts & Active Cards):** Background `#1C2420`, 1px stroke in `#28352F`, with an ambient drop shadow: `0 8px 24px rgba(0, 0, 0, 0.45)`.
- **Level 3 (Modals, Action Drawers):** Background `#222C27`, 1px stroke in `#28352F`, accompanied by `0 16px 40px rgba(0, 0, 0, 0.65)`.
- **Active State / Completed Habit Glow:** When a habit is marked complete or selected, apply a localized radiant aura: `0 0 16px rgba(16, 185, 129, 0.22)`. This provides kinetic validation without cluttering the screen.

## Shapes

The shape hierarchy is unified under Level 2 roundedness (`0.5rem` / 8px base), scaling up to generous radii for prominent card structures to balance structural geometry with human touch.

- **Base Controls (`rounded-sm` / 8px):** Form fields, list item check-triggers, segmented control tabs.
- **Standard Containers (`rounded-lg` / 16px):** Habit cards, calendar summary widgets, bottom sheet panels.
- **Hero Containers (`rounded-xl` / 24px):** Milestone celebratory banners, streak heatmaps, stats breakdown tiles.
- **Capsule Elements (`rounded-full` / 9999px):** Daily streak counters, tag chips, completion checkmarks, circular progress thumbs, and floating action triggers.

## Components

### Buttons
- **Primary CTA:** Background `#10B981`, label in `#0B0F0D` (weight 700), height 52px, border-radius 16px. Active/pressed state transitions to `#059669`. Glow on focus/press: `0 0 14px rgba(52, 211, 153, 0.35)`.
- **Secondary Ghost:** Background `#171F1A`, 1px border `#28352F`, text `#F1F5F9`. Hover/press fills to `#1C2420` with border `#10B981`.
- **Destructive / Reset:** Background transparent, text `#F87171`, border 1px solid rgba(248, 113, 113, 0.2).

### Habit Cards
- **Structure:** Surface `#171F1A`, border 1px solid `#28352F`, rounded-lg (16px), padding `1rem` (16px).
- **Layout:** Leading custom icon container (36x36px, fill `#1C2420`, rounded-md), habit title (`title-md`, `#F1F5F9`), subtitle frequency/category (`body-sm`, `#94A3B8`), and trailing interactive check circle.
- **Completed State:** Card background transitions to `#1C2420`, left border accents with a 3px vertical strip of `#10B981`, and the trailing check button illuminates with emerald fill `#10B981` and black checkmark icon.

### Checkboxes & Habit Check-Triggers
- **Interaction Ring:** Circular trigger (32px diameter). Incomplete state: background `#101512`, 1.5px border `#28352F`.
- **Completed State:** Animate to fill `#10B981` with icon stroke `#0B0F0D` (2px thickness), supported by a momentary emerald micro-pulse.

### Chips & Filter Tags
- **Default:** Background `#101512`, border 1px solid `#28352F`, text `#94A3B8`, rounded-full, padding 6px 12px, font `label-md`.
- **Selected:** Background `#1C2420`, border 1px solid `#34D399`, text `#34D399`.

### Input Fields
- **Container:** Height 48px, background `#101512`, border 1px solid `#28352F`, rounded-md (8px), padding 0 16px.
- **Typography:** Value text `#F1F5F9`, placeholder text `#475569`.
- **Focus State:** Border shifts cleanly to `#10B981` with an interior subtle highlight. No abrupt focus outlines.

### Streak Heatmap & Daily Tracker (Custom Product Component)
- **Grid Tiles:** 7-column matrix for day cycles.
- **Empty Day:** 12x12px rounded-xs (4px) tile, fill `#171F1A`, 1px border `#1E2823`.
- **Partial Complete:** Fill `#059669` at 50% opacity.
- **Completed Day:** Fill `#10B981`, 1px border `#34D399`.
- **Current Day Indicator:** Pulsing 1px ring in `#34D399` encircling today's node.