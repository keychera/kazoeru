(ns chera.num-converter
  (:require [clojure.string :as str]))

;; number conversion based on data/code from several sources
;; currently only integer from 0 to 1e20 - 1, haven't made sure every exception yet
;; -> main source for now: https://www.tofugu.com/japanese/counting-in-japanese/
;; -> for cross checking some logic: https://github.com/Greatdane/Convert-Numbers-to-Japanese/blob/master/Convert-Numbers-to-Japanese.py

;; maths!

(defn remove-last-digit [num]
  (long (Math/floor (/ num 10))))

(defn order10 [num]
  (-> num Math/log10 Math/floor int))

(defn is-last-digit? [inp last-dig]
  (let [last-dig-order (order10 last-dig)
        last-dig-to-compare (int (rem inp (Math/pow 10 (inc last-dig-order))))]
    (= last-dig last-dig-to-compare)))

;; handling 0..10000

(def digit->kango
  {0 "ゼロ" 1 "いち" 2 "に" 3 "さん" 4 "よん" 5 "ご" 6 "ろく" 7 "なな" 8 "はち" 9 "きゅう"})

(def special-1digit-u10000
  {[1 1]  "じゅう"
   [1 2]  "ひゃく"
   [3 2]  "さんびゃく"
   [6 2]  "ろっぴゃく"
   [8 2]  "はっぴゃく"
   [1 3]  "いっせん"
   [3 3]  "さんぜん"
   [8 3]  "はっせん"})

(defn digit-u10000->yomi
  "convert digit u(nder) 10000 to japanese reading"
  [digit order]
  (or (special-1digit-u10000 [digit order])
      (when-not (= digit 0)
        (let [kango (digit->kango digit)]
          (cond (= 3 order) (str kango "せん")
                (= 2 order) (str kango "ひゃく")
                (= 1 order) (str kango "じゅう")
                :else kango)))))

(defn westarabic-u10000->yomi [number]
  (->> (loop [num number order 0
              acc []]
         (let [digit (mod num 10)
               yomi (digit-u10000->yomi digit order)]
           (if (< num 10)
             (conj acc yomi)
             (recur (remove-last-digit num) (inc order)
                    (conj acc yomi)))))
       (reduce #(str %2 %1))))

;; handling every log10eN, data struct {:log1e4 a :num b}, b is 0..10000

(defn special-ld-yomi
  "special l(ast) d(igit) on certain l(og10)e4 order"
  [log1e4 num]
  (let [handle (fn [tail] (fn [ld] {:num (- num ld) :special-num ld :tail tail}))]
    (condp (fn [[le4' ld] [le4 num]]
             (when (and (= le4 le4') (is-last-digit? num ld)) ld))
           [log1e4 num]
      [3  1] :>> (handle {:yomi "いっちょう" :kanji "兆"})
      [3  8] :>> (handle {:yomi "はっちょう" :kanji "兆"})
      [3 10] :>> (handle {:yomi "じゅっちょう" :kanji "兆"})
      [4  1] :>> (handle {:yomi "いっきょう" :kanji "京"})
      [4  6] :>> (handle {:yomi "ろっきょう" :kanji "京"})
      [4  8] :>> (handle {:yomi "はっきょう" :kanji "京"})
      [4 10] :>> (handle {:yomi "じゅっきょう" :kanji "京"})
      nil)))

(def log1e4->kango
  {6 {:yomi "じょ" :kanji "𥝱"}
   5 {:yomi "がい" :kanji "垓"}
   4 {:yomi "きょう" :kanji "京"}
   3 {:yomi "ちょう" :kanji "兆"}
   2 {:yomi "おく" :kanji "億"}
   1 {:yomi "まん" :kanji "万"}})

(def max-inp (-> (apply max (keys log1e4->kango)) (+ 1) (* 4)))

(defn handle-log1e4-yomi [{:keys [log1e4 num] :as data}]
  (or (when (= num 0) data)
      (some-> (special-ld-yomi log1e4 num)
              (as-> special-case
                    (merge data special-case)))
      (assoc data :tail (log1e4->kango log1e4))))

(defn break-log1e4 [inp]
  (loop [^String num inp 
         acc []]
    (let [digit (count num)]
      (cond
        (= inp "") nil
        (<= digit 4) (conj acc {:log1e4 (count acc) :num (js/parseInt num)})
        :else (let [first-order (subs num (- digit 4))
                    remaining-order (subs num 0 (- digit 4))
                    accumulated (conj acc {:log1e4 (count acc) :num (js/parseInt first-order)})]
                (recur remaining-order accumulated))))))

(defn westarab->japanese [^String num]
  (cond
    (or (nil? num) (= "" num)) {:num+kanji "" :reading ""}
    (= num "0") {:raw num :reading "ゼロ" :num+kanji num}
    (str/starts-with? num "-") {:num+kanji "does not support negative number yet!"}
    (str/includes? num ".") {:num+kanji "does not support decimals yet!"}
    (str/includes? num "e") {:num+kanji "does not support e notation yet!"}
    (> (count num) max-inp) {:num+kanji (str "does support number more than 1e" max-inp " - 1 yet!")}
    (not (re-matches #"^\d+$" num)) {:num+kanji "invalid number or unsupported format!"}
      ;; bug cannot get all invalid val from input https://stackoverflow.com/q/40073053/8812880
    :else (as-> num it
            (break-log1e4 it)
            (map handle-log1e4-yomi it)
            (map (fn [res] (assoc res :reading (westarabic-u10000->yomi (:num res)))) it)
            {:num num
             :reading (->> it (map #(str (:reading %) (-> % :tail :yomi))) (reduce #(str %2 %1)))
             :num+kanji (->> it (map (fn [{:keys [num special-num] :as element}]
                                       (let [orig-num (+ num special-num)]
                                         (when-not (= 0 orig-num)
                                           (str orig-num (-> element :tail :kanji)))))) (reduce #(str %2 %1)))})))
