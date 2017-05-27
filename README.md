# Om-CSS A library to help generate co-located CSS on Om and Untangled components.

This library provides some utility functions that help you use 
[garden](https://github.com/noprompt/garden) for co-located, localized
component CSS. 

<a href="https://clojars.org/untangled/om-css">
<img src="https://clojars.org/untangled/om-css/latest-version.svg">
</a>

## Usage

First, co-locate your rules on the components, and use the localized classnames
in your rendering. The primary tools for this are [garden](https://github.com/noprompt/garden) syntax,
the `css/localCSS` protocol for automatically generating localized classname keywords for 
[garden](https://github.com/noprompt/garden) and `css/get-classnames` to generate localized classname strings
for use in the `:className` attribute of DOM elements.

**IMPORTANT NOTE:** The composition rules for CSS are just like Om queries and
Untangled initial app state: it has to all compose to some root, and you obtain
the total result from that root. The obvious disadvantage is that if you forget
to compose it, it won't appear. However, it has the distinct advantage: if you
don't use it, you don't end up emitting it!

### Component samples 

```clj
(ns my-ns
  (:require 
     [om-css.core :as css]
     [om.next :as om :refer-macros [defui]]))
  
(defui Component
  static css/localCSS
  (local-css [this] [ [:.class1 {:color 'orange}] 
                      [:.class2 {:color 'yellow}] ])
  static css/globalCSS    ; Use the globalCSS protocol to prevent keywords from being localized.
  (global-css [this] [ [:class3 {:color 'purple}] ])
  Object
  (render [this]
    (let [{:keys [class1 class2 class3] (css/get-classnames Component)}])
    (localize-classnames Component
       (dom/div #js {:className (str class1 " " class2 " " class3)} ...))))
       
(defui Component2
  static css/localCSS
  (local-css [this] [ [:.class4 {:color 'blue}] ])
  static css/childrenCSS
  ; CSS rules can be composed from children and additional garden rules:
  (children-css [this] [Component]))
```

### Emitting your styles to the page

There are two methods for putting your co-located styles into your 
application:

- Emit a `dom/style` element in your Root UI component. For example:
  `(dom/style nil (garden.core/css (om-css.core/get-css Root)))`. The problem with this
  approach is that your root element itself will not see all of the CSS, since the style is embedded within it.
- Force a style element out to the DOM document. There is a helper function `om-css.core/upsert-css` that can
  be called somewhere in your application initialization. It will extract the CSS and put it in a style element. If that 
  style element already exists then it will replace it, meaning that you can use it in the namespace that figwheel always
  reloads as a way to refresh the CSS during development.

### Allowing external users to customize the CSS rules

One intention of this co-located CSS is to enable component libraries to come with CSS
that is easy to configure and use. Since the CSS is written as code you can use
things like atoms to represent colors, sizes, etc. Simply provide some helper functions
that allow a user to set things like colors and such, and use the resulting values 
in the co-located CSS generation.

# More Information

See the Untangled Cookbook [css recipe](https://github.com/untangled-web/untangled-cookbook/tree/master/recipes/css) for a working example.
