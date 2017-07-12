# Om-CSS A library to help generate co-located CSS on Om and Fulcro components.

This library provides some utility functions that help you use 
[garden](https://github.com/noprompt/garden) for co-located, localized
component CSS. 

<a href="https://clojars.org/fulcrologic/fulcro-css">
<img src="https://clojars.org/fulcrologic/fulcro-css/latest-version.svg">
</a>

## Usage (version 1.1.0 and above)

This library requires `[org.omcljs/om "1.0.0-beta1"]` or above.

A typical file will have the following shape:

```clj
(ns fulcro-css.css-spec
  (:require [om.dom :as dom]
            [fulcro-css.css :as css]
            [om.next :as om :refer [defui]]))

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
  static css/Global
  (global-rules [this] [[:.text {:color "yellow"}]])
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
 and component name to ensure name collisions are impossible. To prevent this localization you can prefix a rule with a `$`  character (`:$container`) instead of a `.` (`:.container`), these rules will *not* be namespaced.
2. Add the `include-children` protocol method. This method MUST return a vector (which can be empty). It should
include the Om component names for any components that are used within the `render` that also supply CSS. This
allows the library to compose together your CSS according to what components you *use*.
3. (optional) Add the `fulcro-css.css/Global` protocol to emit garden rules that will *not* be namespaced. This, any
rule emitted from here will be exactly the name you use.
4. Use the `fulcro-css.css/get-classnames` function to get a map keyed by the simple name you used in your garden rules. 
 The values of the return map are the localized names. This allows you to use the more complex classnames without having to know what
they actually are.
5. Use the `fulcro-css.css/upsert-css` function to embed the CSS.

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

``Garden's selectors`` are supported. These include the *CSS combinators* and the special `&` selector. Using the `$`-prefix will also prevent the selectors from being localized.

```clj
  (local-rules [this] [[(garden.selectors/> :.a :$b) {:color "blue"}]])
```

```html
.namespace_Component__a > .b {
  color: blue;
}
```

# OLD SUPPORT (versions 1.0.2 and below)

The following documentation covers the `core` namespace, which is unchanged from version 1.0.2. This is here so legacy
users can continue to use the original APIs without hassle, and choose when to port to the new `fulcro-css/css` namespace's
API. **New applications should not use the following API.**

## Usage (DEPRECATED)

First, co-locate your rules on the components, and use the localized class
names in your rendering. The primary tools for this are [garden](https://github.com/noprompt/garden) syntax,
`css/local-kw` to generate localized classname keywords for [garden](https://github.com/noprompt/garden),
`css/local-class` to generate localized classname strings for use in
the `:className` attribute of DOM elements, and `localize-classnames`
which is a macro that will rewrite a render body from simple a `:class`
attribute to the proper `:className` attribute.

**IMPORTANT NOTE:** The composition rules for CSS are just like Om queries and
Fulcro initial app state: it has to all compose to some root, and you obtain
the total result from that root. The obvious disadvantage is that if you forget
to compose it, it won't appear. However, it has the distinct advantage: if you
don't use it, you don't end up emitting it!

### Component samples (DEPRECATED)

```clj
(ns my-ns
  (:require 
     [fulcro-css.core :as css :refer-macros [localize-classnames]]
     [om.next :as om :refer-macros [defui]]))
  
(defui Component
  static css/CSS
  (css [this] [ [(css/local-kw Component :class1) {:color 'blue}] 
                [(css/local-kw Component :class2) {:color 'blue}] ])
  Object
  (render [this]
    ; can use a macro to rewrite classnames. $ is used to prevent localization. Note the use of :class instead of :className
    (localize-classnames Component
       (dom/div #js {:class [:class1 :class2 :$root-class]} ...))))
       
(defui Component2
  static css/CSS
  ; CSS rules can be composed from children and additional garden rules:
  (css [this] (css/css-merge 
                 Component 
                 [(css/local-kw Component2 :class) {:color 'red}]))
  Object
  (render [this]
    ; there is a helper function if you just want to get the munged classname
    (dom/div #js {:className (css/local-class Component2 :class) } ...)))
```

### Emitting your styles to the page (DEPRECATED)

There are two methods for putting your co-located styles into your 
application:

- Emit a `dom/style` element in your Root UI component. For example:
  `(dom/style nil (garden.core/css (fulcro-css.core/css Root)))`. The problem with this
  approach is that your root element itself will not see all of the CSS, since the style is embedded within it.
- Force a style element out to the DOM document. There is a helper function `fulcro-css.core/upsert-css` that can
  be called somewhere in your application initialization. It will extract the CSS and put it in a style element. If that 
  style element already exists then it will replace it, meaning that you can use it in the namespace that figwheel always
  reloads as a way to refresh the CSS during development.

### Allowing external users to customize the CSS rules (DEPRECATED)

One intention of this co-located CSS is to enable component libraries to come with CSS
that is easy to configure and use. Since the CSS is written as code you can use
things like atoms to represent colors, sizes, etc. Simply provide some helper functions
that allow a user to set things like colors and such, and use the resulting values 
in the co-located CSS generation.

