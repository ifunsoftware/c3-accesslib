package com.ifunsoftware.c3.access.impl

import collection.mutable.ArrayBuffer

object MetadataHelper{

  private def isSequence(value: String): Boolean = {
    value.charAt(0) == '[' && value.charAt(value.length - 1) == ']'
  }

  def parseSequence(value: String): TraversableOnce[String] = {

    if(!isSequence(value)){
      Some(value)
    }else{

      var valueStart = 1

      val result = new ArrayBuffer[String]

      for (i <- valueStart until value.length - 1){
        if (value.charAt(i) == ',' && value.charAt(i - 1) != '\\'){
          result += value.substring(valueStart, i).replaceAll("\\\\,", ",")
          valueStart = i + 1
        }
      }

      result += value.substring(valueStart, value.length - 1)

      result
    }
  }

}
