# A library to co-locate CSS on Om and Fulcro components.

This library provides some utility functions that help you use 
[garden](https://github.com/noprompt/garden) for co-located, localized
component CSS. 

[![Clojars Project](https://img.shields.io/clojars/v/fulcrologic/fulcro-css.svg)](https://clojars.org/fulcrologic/fulcro-css)

Release [![CircleCI](https://circleci.com/gh/fulcrologic/fulcro-css/tree/master.svg?style=svg)](https://circleci.com/gh/fulcrologic/fulcro-css/tree/master)
Development [![CircleCI](https://circleci.com/gh/fulcrologic/fulcro-css/tree/develop.svg?style=svg)](https://circleci.com/gh/fulcrologic/fulcro-css/tree/develop)

# End of Maintenance Notice

This library has been combined with Fulcro.  It will no longer be
maintained as a stand-alone library.  If a new maintainer would
like to take over for the Om Next community, then please
contact us, and we'll be happy to link to a fork of this repository.

## Usage

A typical file will have the following shape:

Using Fulcro 2.x `defsc`:

```clj
(ns my-ui
  (:require [fulcro.client.dom :as dom]
            [fulcro-css.css :as css]
            [fulcro.client.primitives :as prim :refer [defui defsc]]))

; the item binding is destructured as the fourth param. The actual CSS classname
; will be namespaced to the component as my_ui_ListItem__item, but will be available
; as the value :item in css-classnames map parameter, so you can easily
; destructure if and use it in the DOM without having to worry about how it is prefixed.
(defsc ListItem [this {:keys [label] :as props} computed {:keys [item] :as css-classes}]
  {:css [[:.item {:font-weight "bold"}]]}
  (dom/li #js {:className item} label))

(def ui-list-item (om/factory ListItem {:keyfn :id}))

(defsc ListComponent [this {:keys [id items]} computed {:keys [items-wrapper]}]
  {:css [[:.items-wrapper {:background-color "blue"}]] ; this component's css
   :css-include [ListItem]} ; components whose CSS should be included if this component is included
  (dom/div #js {:className items-wrapper}
    (dom/h2 nil (str "List " id))
    (dom/ul nil (map ui-list-item items))))

(def ui-list (om/factory ListComponent {:keyfn :id}))

(defsc Root [this props computed {:keys [text]}]
  {:css [[:.container {:background-color "red"}]]
   :css-include [ListComponent]}
    (let [the-list {:id 1 :items [{:id 1 :label "A"} {:id 2 :label "B"}]}]
      (dom/div #js {:className text}
        (ui-list the-list))))

; ...

; Add the CSS from Root as a HEAD style element. If it already exists, replace it. This
; will recursively follow all of the CSS includes *just* for components that Root includes!
(css/upsert-css "my-css" Root)
```

Using Om Next-style `defui`:

```clj
(ns fulcro-css.css-spec
  (:require [fulcro.client.dom :as dom]
            [fulcro-css.css :as css]
            [fulcro.client.primitives :as prim :refer [defui defsc]]))

(defui ListItem
  static css/CSS
  (local-rules [this] [[:.item {:font-weight "bold"}]])
  (include-children [this] [])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)
          {:keys [item]} (css/get-classnames ListItem)]
      (dom/li #js {:className item} label))))

(def ui-list-item (om/factory ListItem {:keyfn :id}))

(defui ListComponent
  static css/CSS
  (local-rules [this] [[:.items-wrapper {:background-color "blue"}]])
  (include-children [this] [ListItem])
  Object
  (render [this]
    (let [{:keys [id items]} (om/props this)
          {:keys [items-wrapper]} (css/get-classnames ListComponent)]
      (dom/div #js {:className items-wrapper}
        (dom/h2 nil (str "List " id))
        (dom/ul nil (map ui-list-item items))))))

(def ui-list (om/factory ListComponent {:keyfn :id}))

(defui Root
  static css/CSS
  (local-rules [this] [[:.container {:background-color "red"}]])
  (include-children [this] [ListComponent])
  Object
  (render [this]
    (let [{:keys [text]} (css/get-classnames Root)
          the-list {:id 1 :items [{:id 1 :label "A"} {:id 2 :label "B"}]}]
      (dom/div #js {:className text}
        (ui-list the-list)))))

; ...

; Add the CSS from Root as a HEAD style element. If it already exists, replace it.
(css/upsert-css "my-css" Root)
```

CSS can be co-located on any Om `defui` component. This CSS does *not* take effect until it is embedded on the page 
(see Embedding The CSS below). There are five things to do:
 
1. Add localized rules to your component via the `fulcro-css.css/CSS` protocol `local-rules` method which returns 
 a vector in Garden notation. Any rules included here will be automatically prefixed with the CSSified namespace 
 and component name to ensure name collisions are impossible. 
 *To prevent this localization* you can prefix a rule with a `$`  character (`:$container`) instead of a `.` (`:.container`), 
 these rules will *not* be namespaced.
2. Add the `include-children` protocol method. This method MUST return a vector (which can be empty). It should
include the component names for any components that are used within the `render` that also supply CSS. This
allows the library to compose together your CSS according to what components you *use*.
3. Use the `fulcro-css.css/get-classnames` function to get a map keyed by the simple name you used in your garden rules. 
 The values of the return map are the localized names. This allows you to use the more complex classnames without having to know what
they actually are.
4. Use the `fulcro-css.css/upsert-css` function (or your own style element) to embed the CSS.

In the above example, the upsert results in this CSS on the page:

```html
<style id="my-css">
.fulcro-css_cards-ui_Root__container {
  background-color: red;
}

.text {
  color: yellow;
}

.fulcro-css_cards-ui_ListComponent__items-wrapper {
  background-color: blue;
}

.fulcro-css_cards-ui_ListItem__item {
  font-weight: bold;
}
</style>
```

with a DOM for the UI of:

```html
<div class="text">
  <div class="fulcro-css_cards-ui_ListComponent__items-wrapper">
    <h2>List 1</h2>
    <ul>
      <li class="fulcro-css_cards-ui_ListItem__item">A</li>
      <li class="fulcro-css_cards-ui_ListItem__item">B</li>
    </ul>
  </div>
</div>
```

``Garden's selectors`` are supported. These include the *CSS combinators* and the special `&` selector. Using the 
`$`-prefix will also prevent the selectors from being localized.

```clj
  (local-rules [this] [[(garden.selectors/> :.a :$b) {:color "blue"}]])
```

```html
.namespace_Component__a > .b {
  color: blue;
}
```

