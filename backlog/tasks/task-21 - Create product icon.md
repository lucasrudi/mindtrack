---
id: 21
title: Create product icon
status: Done
priority: medium
labels:
  - frontend
  - design
created: 2026-02-22 00:00
type: feature
dependencies: []
---

## Description

Design and implement a MindTrack product icon for use as favicon, PWA icon, and app branding. The icon should reflect the app's mental health focus — calm, supportive, and professional.

## Plan

1. Define icon concept and style guidelines (color palette, shape language)
2. Design icon in SVG format (scalable, works at multiple sizes)
3. Export icon in required sizes: 16x16, 32x32, 192x192, 512x512 (PWA), 180x180 (Apple touch)
4. Replace existing favicon and add PWA icon references in `index.html` and `vite.config.ts`
5. Add icon to app header/navbar for brand consistency

## Acceptance Criteria

- [ ] SVG master icon file committed to repo
- [ ] Favicon updated in browser tab
- [ ] PWA icons exported at required sizes
- [ ] Icon displayed in app header/navbar
- [ ] Icon renders cleanly at all required sizes
- [ ] No placeholder or default Vite/Vue icons remain
