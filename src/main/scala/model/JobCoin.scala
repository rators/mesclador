package model

object JobCoin {
  def apply(value: BigDecimal): JobCoin = new JobCoin(value)
}

class JobCoin(val value: BigDecimal) {
  override def toString: Address = s"JobCoin(${value.toString})"

  def >(ojc: JobCoin): Boolean = value > ojc.value
  def >=(ojc: JobCoin): Boolean = value >= ojc.value
  def <(ojc: JobCoin): Boolean = value < ojc.value
  def -(ojc: JobCoin): JobCoin = JobCoin(value - ojc.value)
  def +(ojc: JobCoin): JobCoin = JobCoin(value + ojc.value)

}