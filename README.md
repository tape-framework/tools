## README

STATUS: Pre-alpha, in design and prototyping phase.

### About

`tape.tools`

Some common pieces of infrastructure:
- input data helpers
- timeouts & intervals effects

### Install

Add `tape.tools` to your deps:

```clojure
tape/tools {:local/root "../tools"}
```

### Usage

You must be familiar with `tape.module` and `tape.mvc` (particularly the
controller part) before proceeding. Require as:

```clojure
(:require [tape.tools :as tools])
```

#### Ergonomic API

To allow IDE navigation, we have two macros that proxy to Re-Frame:

```clojure
(tools/dispatch [posts.c/index]) ;; => (rf/dispatch [::posts.c/index])
(tools/subscribe [posts.c/posts]) ;; => (rf/subscribe [::posts.c/posts])
```

In their use, the macros accept events with a symbol form (that can be
navigated via IDE), but once compiled, they are in the standard Re-Frame API
with no performance penalty. Added vaue: the handler existance is checked at
compile time, and typos are avoided.

#### The lens pattern

In your view namespace require `tape.tools`:

```clojure
(ns blog.app.posts.view
  (:require [reagent.core :as r]
            [tape.tools :as tools :include-macros true]))
```

A `tools/lens*` is a helper for making cursors that read from a subscription and
write by dispatching an event.

```clojure
(defn form-fields []
  (let [lens (tools/lens* ::posts.c/post ::posts.c/field)
        title (r/cursor lens [:title])]
    [:input {:value @title
             :on-change #(reset! title (-> % .-target .-value))}]))
```

When the `title` cursor above is `reset!`, it dispatches an event:
`[::posts.c/field :title "some value"]`. When it is `deref`'ed it reads the:
`(:title @(rf/subscription [::posts.c/post]))` value.

`tools/cursor` directly returns a Reagent cursor over a lens.

#### Form tools

In your view namespace require `tape.tools.ui.form`:

```clojure
(ns blog.app.posts.view
  (:require [tape.tools.ui.form :as form]))
```

##### Inputs

The `form/field` helper creates an input over a cursor source (ratom or get-set
fn).

```clojure
(defn form-fields []
  (let [lens (tools/lens* ::posts.c/post ::posts.c/field)]
    [:<>
     [form/field {:type :text, :source lens, :field :title}]
     [form/field {:type :textarea, :source lens, :field :description}]]))

```

We assume the following 2 controller functions are present:

```clojure
(ns blog.app.posts.controller ...)
(defn ^::c/event-db field [db [_ k v]] (assoc-in db [::post k] v))
(defn ^::c/sub post [db _] (::post db))
```

##### Lens Ergonomic API

Ergonomic API uses macros with symbols of controller event handlers. Such
symbols can be navigated via IDE "jump to definition". The macros are
macroexpanded in the API above, so there's no performance penalty at runtime.

`(tools/lens posts.c/post posts.c/field)`

##### Validation feedback

The `form/field-with-list-errors` is a drop-in replacement for `form/field`
that adds the class `is-danger` on the input when there are `:errors` on the
input map `m`, and also lists the errors below.

##### Constraint Validation

The `form/when-valid` helper can be used on forms to to check and show the
HTML5 Constraint Validation API.

```clojure
(defn save-button []
  (let [save #(rf/dispatch [::posts.c/save])]
    [:button {:on-click (form/when-valid save)} "Save"]))
```

If the form to which the button above belongs is valid, on clicking the button
the `save` function is called; if the form is invalid the `save` function is
not called and the form validity is reported.

#### Timeouts and Intervals

We have two controllers with event handlers and effect handlers for setting and
clearing timeouts and intervals.

```clojure
(ns my.app
  (:require [tape.tools.timeouts.controller :as timeouts.c]
            [tape.tools.intervals.controller :as intervals.c]))
;; also add ::timeouts.c/module & ::intervals.c/module to the ig config map
```

A timeout is a map with 3 positions: the number of milliseconds, an optional
event that gets dispatched when the timeout is set (with the timeout id added)
and an event that gets dispatched when the timer times out:

```clojure
{:ms 3000
 :set [::was-set] ;; optional, dispatched as [::was-set timeout-id]
 :timeout [::timed-out]}
```

Similarly, an interval:

```clojure
{:ms 3000
 :set [::was-set] ;; dispatched as [::was-set interval-id]
 :interval [::period-reached]}
```

These are given as arguments to events or effects, as follows:

```clojure
[::timeouts.c/set a-timeout] ;; event
[::timeouts.c/clear a-timeout-id] ;; event

{::timeouts.c/set a-timeout} ;; effect
{::timeouts.c/clear a-timeout-id} ;; effect

[::intervals.c/set an-interval] ;; event
[::intervals.c/clear an-interval-id] ;; event

{::intervals.c/set an-interval} ;; effect
{::intervals.c/clear an-interval-id} ;; effect
```

#### License

Copyright © 2019 clyfe

Distributed under the MIT license.
