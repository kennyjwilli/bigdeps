(ns kwill.bigdeps
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pp]
    [clojure.tools.deps.alpha :as deps]
    [clojure.tools.deps.alpha.util.session :as session])
  (:import (java.io File)))

(defn get-dir-size
  [dir]
  (reduce (fn [acc ^File f]
            (if (.isFile f)
              (+ acc (.length f))
              0))
    0.0 (file-seq (io/file dir))))

(comment
  (get-dir-size "."))

(defn big-files-in-dir
  [dir]
  (->> (file-seq (io/file dir))
    (filter #(.isFile %))
    (map (juxt #(.length %) identity))
    (sort-by first)))

(comment
  (big-files-in-dir
    "/Users/kenny/work/computesoftware/monorepo/projects/strategy"))

(defn get-size
  [path]
  (let [file (io/file path)]
    (if (.isFile file)
      (.length file)
      (get-dir-size file))))

(defn find-bigdeps
  [{:keys [deps]
    :or   {deps "deps.edn"}}]
  (let [the-dir (.getParentFile (.getAbsoluteFile (io/file deps)))
        {:keys [root-edn user-edn project-edn]} (deps/find-edn-maps deps)
        master-edn (deps/merge-edns [root-edn user-edn project-edn])
        aliases []
        combined-aliases (deps/combine-aliases master-edn aliases)
        basis (binding [clojure.tools.deps.alpha.util.dir/*the-dir* the-dir]
                (session/with-session
                  (deps/calc-basis master-edn {:resolve-args   (merge combined-aliases {:trace true})
                                               :classpath-args combined-aliases})))
        {:keys [classpath libs]} basis]
    ;(def basis basis)
    (->> classpath
      (map (fn [[path {:keys [lib-name] :as src}]]
             {:size       (get-size path)
              :path       path
              :src        src
              :dependents (get-in libs [lib-name :dependents])}))
      (sort-by :size #(compare %2 %1)))))

(comment (.getParentFile (.getAbsoluteFile (io/file "deps.edn"))))

(defn print!
  [{:keys [bigdeps n]}]
  (pp/print-table [:size-mb :lib-name :dependents]
    (->> bigdeps
      (take (or n Integer/MAX_VALUE))
      (map (fn [{:keys [size path src dependents]}]
             {:size-mb    (format "%.2f" (double (/ size (* 1024 1024))))
              :lib-name   (:lib-name src)
              :dependents dependents})))))

(comment
  (def big (find-bigdeps {}))
  (print! {:bigdeps big})
  )

(defn run
  [opts]
  (let [bigdeps (find-bigdeps opts)]
    (print! (assoc opts :bigdeps bigdeps))))
