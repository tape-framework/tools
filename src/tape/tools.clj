(ns tape.tools
  (:require [tape.mvc.meta :as meta]))

(defmacro lens
  "Like `lens*` but takes symbol args and translates to keywords. Example:
  `(tools/lens posts.c/post posts.c/field)`."
  ([sym]
   `(lens* ~(meta/sub-kw &env sym) ~(meta/event-kw &env sym)))
  ([sget sset]
   `(lens* ~(meta/sub-kw &env sget) ~(meta/event-kw &env sset))))
