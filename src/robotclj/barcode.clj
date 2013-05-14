(ns robotclj.barcode)

(defn to-int
  "Converts a barcode string represented by ones and zeros to an
integer representation. The integer is the binary number represented by the ones
and zeroes"
  [s]
  (Integer/parseInt s 2))

(defn to-string
  "Converts a barcode int to a string representation"
  [i]
  :TODO )


(defn seesaw-barcode?
  "Returns whether the barcode represents a seesaw"
  [b]
  (#{11,13,15,17} b))

(defn object-barcode?
  "Returns whether the barcode represents an object"
  [b]
  (#{1,2,3,4} b)
