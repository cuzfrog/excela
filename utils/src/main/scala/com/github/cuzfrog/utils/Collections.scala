package com.github.cuzfrog.utils



private[cuzfrog] object Collections {
  implicit class ExMap[A, B](val in: Map[A, B]) {
    /**
     * Ex-version of valueMap, which return a new map instead of an evil view.
     */
    def valueMap[C](f: B => C): Map[A, C] = {
      in.map { case (k, v) => (k, f(v)) }
    }

    /**
     * Ex-version of filterKeys, which return a new map instead of an evil view.
     */
    def keyFilter(f: A => Boolean): Map[A, B] = {
      in.filter(e => f(e._1))
    }

    /**
     * Filter a map by values.
     */
    def valueFilter(f: B => Boolean): Map[A, B] = {
      in.filter(e => f(e._2))
    }

    /**
     * Left join two map by keys and interact associated values with specified function.
     * Keys in left map but not in right map are preserved with their values processed by a supplement function.
     * Keys in right map but not in left map are discarded.
     * @param that the right Map to be joined.
     * @param f function through which two associated values interact.
     * @param supplement function that is used when a key in the left Map is not in the right Map.
     * @return result Map.
     */
    def leftJoin[B2, C](that: Map[A, B2])(f: (B, B2) => C)(supplement: B => C): Map[A, C] = {
      in.map {
        e =>
          val key = e._1
          val b1 = e._2
          val c = that.get(key) match {
            case Some(b2) => f(b1, b2)
            case None     => supplement(b1)
          }
          key -> c
      }
    }

    /**
     * Inner join two maps and interact associated values with specified function.
     * Only keys in both left map and right map are preserved. Other keys are discarded.
     * <br>
     * Note: order of result map key may not be the same as left map. To preserve order, use {@link leftJoin}
     */
    def innerJoin[B2, C](that: Map[A, B2])(f: (B, B2) => C): Map[A, C] = {
      val keys = in.keySet.intersect(that.keySet) //use Seq to preserve keys' order
      keys.map {
        key =>
          val c = f(in(key), that(key))
          key -> c
      }.toMap
    }
  }

  implicit class ExSeq[A](in: Seq[A]) {
    
  }

  implicit class TableT4[A, B, C, D](in: Seq[(A, B, C, D)]) {
    def toTree: Map[A, Map[B, Map[C, D]]] = {
      val out = in.view.groupBy(_._1).valueMap {
        b =>
          b.groupBy(_._2).valueMap {
            c => c.groupBy(_._3).valueMap(_.head._4)
          }
      }
      out.view.force
    }
  }

  implicit class TableT3[A, B, C](in: Seq[(A, B, C)]) {
    def toTree: Map[A, Map[B, C]] = {
      val out = in.view.groupBy(_._1).valueMap {
        b => b.groupBy(_._2).valueMap(_.head._3)
      }
      out.view.force
    }
  }
}