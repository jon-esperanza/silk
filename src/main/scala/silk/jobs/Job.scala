package jesperan.silk
package jobs

class Job (var name: String, var function: Function[List[String], Unit]) {
  def execute(data: List[String]): Unit = {
    function(data)
  }
}
