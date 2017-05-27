(ns om-css.css
  (:require [cljs.tagged-literals]
            [clojure.string :as str]
            [com.rpl.specter :as sp]
            [om.next :as om]
            [garden.core :as g]
            [cljs.core]
            [om-css.core :as oc]))

(defprotocol CSS
  (local-rules [this] "Specifies the component's local CSS rules")
  (include-children [this] "Specifies the components (typically direct children) whose CSS should be included."))

(defprotocol Global
  (global-rules [this] "Specifies the component's global CSS rules"))

#?(:clj (defn implements-protocol?
          [x protocol protocol-key]
          (if (fn? x)
            (some? (-> x meta protocol-key))
            (extends? protocol (class x)))))

(defn CSS?
  "Returns true if the given component has css"
  [x]
  #?(:clj (implements-protocol? x CSS :local-rules)
     :cljs (implements? CSS x)))

(defn Global?
  "Returns true if the component has global rules"
  [x]
  #?(:clj (implements-protocol? x Global :global-rules)
     :cljs (implements? Global x)))

(defn get-global-rules
  "Get the *raw* value from the global-rules of a component."
  [component]
  (if (Global? component)
    #?(:clj ((:global-rules (meta component)) component)
       :cljs (global-rules component))
    []))

(defn get-local-rules
  "Get the *raw* value from the local-rules of a component."
  [component]
  (if (CSS? component)
    #?(:clj ((:local-rules (meta component)) component)
       :cljs (local-rules component))
    []))

(defn- prefixed-keyword?
  [kw]
  (and (keyword? kw)
       (str/starts-with? (name kw) ".")))

(defn- remove-prefix
  [kw]
  (keyword (subs (name kw) 1)))

(defn get-includes
  "Returns the list of components from the include-children method of a component"
  [component]
  (if (CSS? component)
    #?(:clj ((:include-children (meta component)) component)
       :cljs (include-children component))
    []))

(defn get-nested-includes
  "Recursively finds all includes starting at the given component."
  [component]
  (let [direct-children (get-includes component)]
    (if (empty? direct-children)
      []
      (concat direct-children (reduce #(concat %1 (get-nested-includes %2)) [] direct-children)))))

(defn- localize-css
  "Converts the simples names specified by the component into localized css names."
  [component]
  (sp/transform (sp/walker prefixed-keyword?)
                #(let [nm (subs (name %) 1)]
                   (oc/local-kw component (keyword nm)))
                (get-local-rules component)))

(defn- get-css-rules
  "Gets the local and global rules from the given component."
  [component]
  (concat (localize-css component) (get-global-rules component)))

(defn get-css
  "Recursively gets all global and localized rules (in garden notation) starting at the given component."
  [component]
  (let [own-rules (get-css-rules component)
        nested-children (distinct (get-nested-includes component))
        nested-children-rules (reduce #(into %1 (get-css-rules %2)) [] nested-children)]
    (concat own-rules nested-children-rules)))

(defn get-classnames
  "Returns a map from user-given CSS rule names to om-css localized names of the given component."
  [comp]
  (let [local-kws (mapv remove-prefix (filter prefixed-keyword? (flatten (get-local-rules comp))))
        global-kws (mapv remove-prefix (filter prefixed-keyword? (flatten (get-global-rules comp))))
        local-classnames (zipmap local-kws (map #(oc/local-class comp %) local-kws))
        global-classnames (zipmap global-kws (map name global-kws))]
    (merge local-classnames global-classnames)))

#?(:cljs
   (defn upsert-css
     "(Re)place the STYLE element with the provided ID on the document's DOM  with the co-located CSS of the specified component."
     [id root-component]
     (oc/remove-from-dom id)
     (let [style-ele (.createElement js/document "style")]
       (set! (.-innerHTML style-ele) (g/css (get-css root-component)))
       (.setAttribute style-ele "id" id)
       (.appendChild (.-body js/document) style-ele))))

