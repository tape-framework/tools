(ns tape.tools.current.controller-test
  (:require [cljs.test :refer [deftest is are run-tests]]
            [integrant.core :as ig]
            [re-frame.core :as rf]
            [tape.module :as module :include-macros true]
            [tape.mvc.controller :as c]
            [tape.mvc.view :as v]
            [tape.mvc :as mvc]
            [tape.tools.current.controller :as current.c]
            [tape.tools.app.controller :as app.c]
            [tape.tools.app.view :as app.v]))

(module/load-hierarchy)

(deftest derive-test
  (are [x y] (isa? x y)
    ::current.c/view ::c/sub
    ::current.c/view-fn ::c/sub))

(defn ^:private view [] [:p "p"])
(def ^:private views {:some.c/event view})

(deftest interceptor-test
  (let [interceptor (ig/init-key ::current.c/view-interceptor views)
        afterf (:after interceptor)
        context {:coeffects {:event [:some.c/event]}
                 :effects {:db {}}}
        context2 (assoc-in context [:coeffects :event 0] :some.c/no-view)]
    (is (= :some.c/event
           (-> context afterf (rf/get-effect :db) ::current.c/view)))
    (is (nil? (-> context2 afterf (rf/get-effect :db) ::current.c/view)))))

(deftest current-fn-test
  (let [current-fn (ig/init-key ::current.c/view-fn views)
        db {::current.c/view :some.c/event}
        db2 {}] (is (= view (current-fn db nil)))
                (is (= nil (current-fn db2 nil)))))

(def ^:private config
  {::c/module nil
   ::v/module nil
   ::mvc/module nil
   ::current.c/module nil
   ::app.c/module nil
   ::app.v/module nil})

(def system (-> config
                module/prep-config
                (ig/init [:tape.mvc/main :tape/multi])))

(deftest reg-test
  (let [system (-> config
                   module/prep-config
                   (ig/init [:tape.mvc/main :tape/multi]))]
    (rf/dispatch-sync [::app.c/event-db])
    (is (= ::app.c/event-db @(rf/subscribe [::current.c/view])))
    (is (= app.v/event-db (.-afn @(rf/subscribe [::current.c/view-fn]))))
    (rf/dispatch-sync [::app.c/goodbye])
    (is (= ::app.c/event-db @(rf/subscribe [::current.c/view])))
    (is (= app.v/event-db (.-afn @(rf/subscribe [::current.c/view-fn]))))
    (ig/halt! system)))

(run-tests)
