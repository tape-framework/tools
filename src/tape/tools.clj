(ns tape.tools
  (:require [tape.mvc.view :as v]))

(defmacro lens
  "Like `lens*` but takes symbol args and translates to keywords. Example:
  `(tools/lens posts.c/post posts.c/field)`."
  ([sym]
   `(lens* ~(v/sub-kw &env sym) ~(v/event-kw &env sym)))
  ([sget sset]
   `(lens* ~(v/sub-kw &env sget) ~(v/event-kw &env sset))))
