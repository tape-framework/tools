(ns tape.tools.ui.form
  "Form helpers to be used in views."
  (:require [reagent.core :as r]))

;;; DOM Values

(defn- value [event] (-> event .-target .-value))

(defn- multiple-value [event]
  (let [options (-> event .-target .-options)]
    (for [i (range 0 (.-length options))
          :let [option (aget options i)]
          :when (.-selected option)]
      (.-value option))))

;;; Input Tag

(defmulti ^:private select-tag* :multiple)

(defmethod select-tag* :default [m]
  (let [{:keys [options include-blank]} m]
    [:select (dissoc m :type :options :include-blank)
     (when include-blank [:option])
     (for [[k v] options] [:option {:key k, :value k} v])]))

(defmethod select-tag* true [m]
  (let [{:keys [options include-blank value]} m
        selected?      (set value)
        if-kw->str     (fn [v] (cond-> v (keyword? v) name))
        select?        (fn [m] (selected? (if-kw->str (:value m))))
        maybe-selected (fn [m] (cond-> m (select? m) (assoc :selected true)))]
    [:select (dissoc m :type :options :include-blank :value)
     (when include-blank [:option (maybe-selected {:value ""})])
     (for [[k v] options]
       [:option (maybe-selected {:key k, :value k}) v])]))

(defn- type-kw [m]
  (let [typ (:type m)]
    (cond-> typ (string? typ) keyword)))

(defmulti input-tag
  "Given an options map with a type, returns a Hiccup tag for it. Ex:
  `(input-tag {:type :textarea})`."
  type-kw)

(defmethod input-tag :default [m] [:input m])
(defmethod input-tag :textarea [m] [:textarea (dissoc m :type)])
(defmethod input-tag :select [m] (select-tag* m))

;;; Input

(defn- pick* [m]
  (let [{:keys [state value]} m
        checked   (= value @state)
        on-change #(reset! state (when-not checked value))
        defaults  {:checked checked, :on-change on-change}]
    [:input (merge defaults (dissoc m :state))]))

(defmulti input
  "Given an options map with a type and a reagent atom, returns a Hiccup
  input for it. Ex: `(input {:type :number :state state})`."
  type-kw)

(defmethod input :default [m]
  (let [{:keys [state]} m
        on-change #(reset! state (value %))
        defaults  {:value (or @state ""), :on-change on-change}]
    (input-tag (merge defaults (dissoc m :state)))))

(defmethod input :checkbox [m] (pick* m))
(defmethod input :radio [m] (pick* m))

(defmethod input :select [m]
  (let [{:keys [state multiple]} m
        get-value (if multiple multiple-value value)
        on-change #(reset! state (get-value %))
        defaults  {:value (or @state ""), :on-change on-change}]
    (input-tag (merge defaults (dissoc m :state)))))

;;; Input

(defn field
  "Given an options map with a type, a reagent cursor source and a field,
  returns a Hiccup input for it. Ex: `(input {:type :number :source get-set})`."
  [m]
  (let [cursor (r/cursor (:source m) [(:field m)])]
    (input (-> m (dissoc :field :source) (assoc :state cursor)))))

(defn field-with-errors [m]
  (let [{:keys [errors errors-class]} m
        errors-class' (or errors-class "is-danger")
        add-errors-class (fn [klass]
                           (cond
                             (vector? klass) (conj klass errors-class')
                             (string? klass) (str klass " " errors-class')
                             :else (throw (ex-info "Unknown class" {}))))
        m' (-> m
               (dissoc :errors :errors-class)
               (cond->
                (some? errors) (update :class add-errors-class)))]
    [field m']))

(defn list-errors [^PersistentHashSet errors]
  (for [error errors]
    [:p.help.is-danger {:key error} error]))

(defn field-with-list-errors [m]
  [:<>
   [field-with-errors m]
   (list-errors (:errors m))])

;;; Constraint Validation

(defn- when-valid* [event f]
  (let [form (-> event .-target .-form)]
    (.preventDefault event)
    (if (.checkValidity form) (f) (.reportValidity form))))

(defn when-valid
  "Wraps the event handler `f` and calls it only if HTML5 Validation API is true
  on the form corresponding to the event. Ex:
  `[:button {:on-click (when-valid #(rf/dispatch [::save]))} \"Save\"]`."
  [f] #(when-valid* % f))
