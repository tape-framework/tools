(ns tape.tools.intervals.controller-test
  (:refer-clojure :exclude [set])
  (:require [cljs.test :refer [deftest is use-fixtures]]
            [day8.re-frame.test :as rft]
            [integrant.core :as ig]
            [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]
            [tape.mvc.view :as v]
            [tape.module :as module :include-macros true]
            [tape.tools.intervals.controller :as intervals.c]))

(module/load-hierarchy)

(defn set
  {::c/reg ::c/event-db}
  [db [_ id]]
  (assoc db ::interval-id id
            ::interval-count 0))

(defn interval
  {::c/reg ::c/event-db}
  [db _] (update db ::interval-count inc))

(defn interval-id
  {::c/reg ::c/sub}
  [db _] (::interval-id db))

(defn interval-count
  {::c/reg ::c/sub}
  [db _] (::interval-count db))

(c/defmodule)

(def ^:private config
  {:tape.profile/base   {}
   ::c/module           nil
   ::v/module           nil
   ::intervals.c/module nil
   ::module             nil})

(def ^:private system nil)

(use-fixtures :once
  {:before (fn [] (set! system (-> config module/prep-config ig/init)))
   :after  (fn [] (ig/halt! system))})

(deftest set-test
  (rft/run-test-async
   (let [interval       {:ms 1, :set [::set], :interval [::interval]}
         interval-id    (rf/subscribe [::interval-id])
         interval-count (rf/subscribe [::interval-count])]
     (rf/dispatch [::intervals.c/set interval])
     (rft/wait-for [::interval]
       (is (integer? @interval-id))
       (is (= 1 @interval-count))
       (rft/wait-for [::interval]
         (is (= 2 @interval-count)))))))

(deftest clear-test
  (rft/run-test-sync
   (let [interval {:ms       1
                   :set      [::intervals.c/clear]
                   :interval [::does-not-dispatch]}
         id       (atom nil)]
     (with-redefs [js/setInterval   (constantly 42)
                   js/clearInterval #(reset! id %)]
       (rf/dispatch [::intervals.c/set interval])
       (is (= 42 @id))))))
