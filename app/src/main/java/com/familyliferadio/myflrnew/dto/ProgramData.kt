package com.familyliferadio.myflrnew.dto

import java.io.Serializable

/**
 * Created by ntf-19 on 6/9/18.
 */
class ProgramData : Serializable {
    var programTitle: String = ""
    var time: String = ""
    var programe_logo: String = ""
    var program_dec: String = ""

    constructor(programTitle: String, time: String, programe_logo: String, program_dec: String) {
        this.programTitle = programTitle
        this.time = time
        this.programe_logo = programe_logo
        this.program_dec = program_dec
    }
}