(ns ui.designer.theming)

(defrecord Typography [font-family font-size font-weight font-style])

(defrecord Palette [main])

(defrecord PaletteSettings [primary secondary tertiary])

(defrecord Theme [palettes typography spacing])

; Macro to create stylized elements
; - div for each palette/color combo
;
; Standardized styling system based on ->> operator
; Styles are functions
; Needs to support clj-kondo linting