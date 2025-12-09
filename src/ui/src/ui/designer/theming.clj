(ns ui.designer.theming)

; Macro to create stylized elements
; - div for each palette/color combo
;
; Standardized styling system based on ->> operator
; Styles are functions
; Needs to support clj-kondo linting


(defmacro defcolor [name hexcode]
  `(do
     (def ~(symbol (str "color-" name)) ~hexcode)
     (def ~(symbol (str name "-bg"))
       #(assoc % :background-color ~hexcode))
     (def ~(symbol (str name "-text"))
       #(assoc % :color ~hexcode))))


(defmacro deftheme [theme-palette theme-typography]
  `(do
     (def ~'palette ~theme-palette)
     (def ~'typography ~theme-typography)))