(ns om-css.css-spec
  (:require #?(:cljs [untangled-spec.core :refer-macros [specification assertions behavior]]
               :clj [untangled-spec.core :refer [specification assertions behavior]])
                    [om-css.css :as css]
                    [om.next :as om :refer [defui]]
                    [om.dom :as dom]
                    [garden.selectors :as sel]))

(defui ListItem
  static css/CSS
  (local-rules [this] [[:.item {:font-weight "bold"}]])
  (include-children [this] [])
  Object
  (render [this]
    (let [{:keys [item]} (css/get-classnames ListItem)]
      (dom/li #js {:className item} "listitem"))))

(defui ListComponent
  static css/CSS
  (local-rules [this] [[:.items-wrapper {:background-color "blue"}]])
  (include-children [this] [ListItem])
  Object
  (render [this]
    (let [{:keys [items-wrapper]} (css/get-classnames ListComponent)]
      (dom/ul #js {:className items-wrapper} "list"))))

(defui Root
  static css/CSS
  (local-rules [this] [[:.container {:background-color "red"}]])
  (include-children [this] [ListComponent])
  static css/Global
  (global-rules [this] [[:.text {:color "green"}]])
  Object
  (render [this]
    (dom/div nil "root")))

(defui Child1
  static css/CSS
  (local-rules [this] [[:.child1class {:color "red"}]])
  (include-children [this] [])
  Object
  (render [this]
    (dom/div nil "test")))

(defui Child2
  static css/CSS
  (local-rules [this] [[:.child2class {:color "blue"}]])
  (include-children [this] [])
  Object
  (render [this]
    (dom/div nil "test")))

(defui Parent
  static css/CSS
  (local-rules [this] [])
  (include-children [this] [Child1 Child2])
  Object
  (render [this]
    (dom/div nil "test")))

(defui MyLabel
  static css/CSS
  (local-rules [this] [[:.my-label {:color "green"}]])
  (include-children [this] []))

(defui MyButton
  static css/CSS
  (local-rules [this] [[:.my-button {:color "black"}]])
  (include-children [this] [MyLabel])
  Object
  (render [this]
    (dom/div nil "test")))

(defui MyForm
  static css/CSS
  (local-rules [this] [[:.form {:background-color "white"}]])
  (include-children [this] [MyButton])
  Object
  (render [this]
    (dom/div nil "test")))

(defui MyNavigation
  static css/CSS
  (local-rules [this] [[:.nav {:width "100px"}]])
  (include-children [this] [MyButton])
  Object
  (render [this]
    (dom/div nil "test")))

(defui MyRoot
  static css/CSS
  (local-rules [this] [])
  (include-children [this] [MyForm MyNavigation])
  Object
  (render [this]
    (dom/div nil "test")))

(specification "Obtain CSS from classes"
  (behavior "can be obtained from"
    (assertions
      "a single component"
      (css/get-css ListItem) => '([:.om-css_css-spec_ListItem__item {:font-weight "bold"}])
      "a component with a child"
      (css/get-css ListComponent) => '([:.om-css_css-spec_ListComponent__items-wrapper {:background-color "blue"}]
                                        [:.om-css_css-spec_ListItem__item {:font-weight "bold"}])
      "a component with nested children"
      (css/get-css Root) => '([:.om-css_css-spec_Root__container {:background-color "red"}]
                               [:.text {:color "green"}]
                               [:.om-css_css-spec_ListComponent__items-wrapper {:background-color "blue"}]
                               [:.om-css_css-spec_ListItem__item {:font-weight "bold"}])
      "a component with multiple direct children"
      (css/get-css Parent) => '([:.om-css_css-spec_Child1__child1class {:color "red"}]
                                 [:.om-css_css-spec_Child2__child2class {:color "blue"}])
      "a component with multiple direct children without duplicating rules"
      (css/get-css MyRoot) => '([:.om-css_css-spec_MyForm__form {:background-color "white"}]
                                 [:.om-css_css-spec_MyNavigation__nav {:width "100px"}]
                                 [:.om-css_css-spec_MyButton__my-button {:color "black"}]
                                 [:.om-css_css-spec_MyLabel__my-label {:color "green"}]))))

(specification "Generate classnames from CSS"
  (assertions
    "global classnames are untouched"
    (:text (css/get-classnames Root)) => "text"
    "local classnames are transformed"
    (:container (css/get-classnames Root)) => "om-css_css-spec_Root__container"
    "does not generate children-classnames"
    (:items-wrapper (css/get-classnames Root)) => nil))

(defui A
  static css/CSS
  (local-rules [this] [[(sel/> :.a :.b :.c) {:color "blue"}]])
  (include-children [this] []))

(defui B
  static css/CSS
  (local-rules [this] [[(sel/> :$a :.b :span :$c) {:color "red"}]])
  (include-children [this] []))

(defui C
  static css/CSS
  (local-rules [this] [[(sel/+ :.a :$b) {:color "green"}]])
  (include-children [this] []))

(defui D
  static css/CSS
  (local-rules [this] [[(sel/- :.a :.b) {:color "yellow"}]])
  (include-children [this] []))

(defui E
  static css/CSS
  (local-rules [this] [[(sel/+ :.a (sel/> :$b :span)) {:color "brown"}]])
  (include-children [this] []))

(defui F
  static css/CSS
  (local-rules [this] [[(sel/+ :.a (sel/> :$b :span)) {:color "brown"}]])
  (include-children [this] [])
  static css/Global
  (global-rules [this] [[(sel/> :.c :.d) {:color "blue"}]]))

(specification "CSS Combinators"
  (assertions
      "Child selector"
    (css/get-css A) => '([#garden.selectors.CSSSelector{:selector ".om-css_css-spec_A__a > .om-css_css-spec_A__b > .om-css_css-spec_A__c"} {:color "blue"}])
    "Child selector with localization prevention"
    (css/get-css B) => '([#garden.selectors.CSSSelector{:selector ".a > .om-css_css-spec_B__b > span > .c"} {:color "red"}])
    "Adjacent sibling selector"
    (css/get-css C) => '([#garden.selectors.CSSSelector{:selector ".om-css_css-spec_C__a + .b"} {:color "green"}])
    "General sibling selector"
    (css/get-css D) => '([#garden.selectors.CSSSelector{:selector ".om-css_css-spec_D__a ~ .om-css_css-spec_D__b"} {:color "yellow"}])
    "Multiple different selectors"
    (css/get-css E) => '([#garden.selectors.CSSSelector{:selector ".om-css_css-spec_E__a + .b > span"} {:color "brown"}])
    "Get classnames"
    (css/get-classnames F) => {:a "om-css_css-spec_F__a"
                               :b "b"
                               :c "c"
                               :d "d"}))

(defui G
  static css/CSS
  (local-rules [this] [[:.a {:color "orange"}
                        [:&.b {:font-weight "bold"}]
                        [:&$c {:background-color "black"}]]])
  (include-children [this] [])
  static css/Global
  (global-rules [this] [[:.d {:color "green"}
                         [:&.e {:color "gray"}]]]))

(specification "Special &-selector"
  (assertions
      "Get CSS rules"
    (css/get-css G) => '([:.om-css_css-spec_G__a {:color "orange"}
                         [:&.om-css_css-spec_G__b {:font-weight "bold"}]
                         [:&.c {:background-color "black"}]]
                        [:.d {:color "green"}
                         [:&.e {:color "gray"}]])
    "Get classnames"
    (css/get-classnames G) => {:a "om-css_css-spec_G__a"
                               :b "om-css_css-spec_G__b"
                               :c "c"
                               :d "d"
                               :e "e"}))




