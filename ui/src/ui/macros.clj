(ns ui.macros)


(defmacro defcolor [name hexcode]
  `(do
     (def ~(symbol (str "color-" name)) ~hexcode)
     (def ~(symbol (str name "-bg"))
       #(assoc % :background-color ~hexcode))
     (def ~(symbol (str name "-text"))
       #(assoc % :color ~hexcode))))
