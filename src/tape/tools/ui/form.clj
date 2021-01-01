(ns tape.tools.ui.form
  (:require [tape.mvc :as mvc]))

(defmacro lens
  "Like `lens*` but takes symbol args and translates to keywords. Example:
  `(tools/lens posts.c/post posts.c/field)`."
  ([sym]
   `(lens* ~(mvc/sub-kw &env sym) ~(mvc/event-kw &env sym)))
  ([sget sset]
   `(lens* ~(mvc/sub-kw &env sget) ~(mvc/event-kw &env sset))))
