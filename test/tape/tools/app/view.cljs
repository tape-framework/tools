(ns tape.tools.app.view
  (:require [tape.mvc :as mvc :include-macros true]
            [tape.tools.app.controller]))

;;; Views

(defn event-db
  {::mvc/reg ::mvc/view}
  [] [:p "p"])

;;; Module

(mvc/defm ::module)
