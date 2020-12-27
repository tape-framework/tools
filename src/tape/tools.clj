(ns tape.tools
  (:require [tape.mvc.meta :as meta]))

;;; Ergonomic API

(defmacro dispatch
  "Like `re-frame.core/dispatch` but with  an IDE navigable symbol instead a
  keyword. When compiled it replaces the symbol with the keyword. Example:
  `(v/dispatch [counter.c/increment])
  ; => (rf/subscribe [::counter.c/increment])`."
  [[fsym & args]]
  `(re-frame.core/dispatch ~(into [(meta/event-kw &env fsym)] args)))

(defmacro subscribe
  "Like `re-frame.core/subscribe` but with  an IDE navigable symbol instead a
  keyword. When compiled it replaces the symbol with the keyword. Example:
  `(v/subscribe [counter.c/count]) ; => (rf/subscribe [::counter.c/count])`."
  [[fsym & args]]
  `(re-frame.core/subscribe ~(into [(meta/sub-kw &env fsym)] args)))

;;; Lens

(defmacro lens
  "Like `lens*` but takes symbol args and translates to keywords. Example:
  `(tools/lens posts.c/post posts.c/field)`."
  ([sym]
   `(lens* ~(meta/sub-kw &env sym) ~(meta/event-kw &env sym)))
  ([sget sset]
   `(lens* ~(meta/sub-kw &env sget) ~(meta/event-kw &env sset))))
