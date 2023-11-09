package org.bigraph.bigsim.parser

import java.util.Objects

import com.google.common.base.Splitter
import org.apache.commons.lang3.StringUtils

object HmmDataParser {

  def parseHMMData(str: String)={
    var tag: String = ""
    val strings = Splitter.on('\n')
      .trimResults()
      .omitEmptyStrings()
      .splitToList(str)

    //    strings.forEach(t => println(t))

    var transition_probability_array: Array[Array[Double]] = Array()
    var emission_probability_array: Array[Array[Double]] = Array()
    var start_probability_array: Array[Double] = Array()
    var states_array: Array[String] = Array()
    var observations_array: Array[String] = Array()
    var observer_seq_array: Array[Int] = Array()

    strings.forEach(s => {
      if(Objects.equals("A_Array", s)){
        tag = "transition_probability"
      }else if(Objects.equals("B_Array", s)){
        tag = "emission_probability"
      }else if(Objects.equals("PI_Array", s)){
        tag = "start_probability"
      }else if(Objects.equals("Hidden_Array", s)){
        tag = "states"
      }else if(Objects.equals("Observer_Array", s)){
        tag = "observations"
      }else if(Objects.equals("Observer_Seq_Array", s)){
        tag = "observer_seq"
      }else if(StringUtils.isNotBlank(s)){

        if(tag.eq("transition_probability")){
          //          println("transition_probability:" + s)
          transition_probability_array = transition_probability_array.:+(parseStringToListDouble(s))

          //          val list = parseStringToListDouble(s)
          //          val array = list.map(_.toDouble).toArray
          //                    println("array -> " + array)
          //          array.foreach(t => println(t))

        }else if(tag.eq("emission_probability")){
          //          println("emission_probability:" + s)
          emission_probability_array = emission_probability_array.:+(parseStringToListDouble(s))

        }else if(tag.eq("start_probability")){
          //          println("start_probability:" + s)
          start_probability_array = parseStringToListDouble(s)

        }else if(tag.eq("states")){
          //          println("states:" + s)
          states_array = parseStringToListString(s)

        }else if(tag.eq("observations")){
          //          println("observations:" + s)
          observations_array = parseStringToListString(s)
        } else if(tag.eq("observer_seq")){
          var tmp: Array[Int] = Array()
          Splitter.on(",").trimResults()
            .split(s)
            .forEach(t => { tmp = tmp.:+(t.toInt) })
          observer_seq_array = tmp
        }
      }
    })

    //    println("tpa -> " + transition_probability_array)
    //    println("epa -> " + emission_probability_array)
    //    println("spa -> " + start_probability_array)
    //    println("sa -> " + states_array)
    //    println("oa -> " + observations_array)

    val hmmData = HmmData(transition_probability_array,
      emission_probability_array,
      start_probability_array,
      states_array,
      observations_array,
      observer_seq_array)

    hmmData

  }

  def parseStringToListDouble(str: String)  : Array[Double] = {
    var tmp: Array[Double] = Array()
    Splitter.on(",").trimResults()
      .split(str)
      .forEach(t => { tmp = tmp.:+(t.toDouble) })
    tmp
  }
  def parseStringToListString(str: String)  : Array[String] = {
    var tmp: Array[String] = Array()
    Splitter.on(",").trimResults()
      .split(str)
      .forEach(t => { tmp = tmp.:+(t) })
    tmp
  }
}
