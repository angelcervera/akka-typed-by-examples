package example

package object server {

  /**
    * Calculate pi until System.nanoTime is later than `endNanos`
    * Borrowed from the Lightbend Akka course.
    */
  def pi(delayNanos: Long) = {
    val endNanos = System.nanoTime() + delayNanos
    def gregoryLeibnitz(n: Long) = 4.0 * (1 - (n % 2) * 2) / (n * 2 + 1)
    var n = 0
    var acc = BigDecimal(0.0)
    while (System.nanoTime() < endNanos) {
      acc += gregoryLeibnitz(n)
      n += 1
    }
    acc
  }
}
