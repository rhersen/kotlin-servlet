import org.jetbrains.spek.api.Spek
import java.nio.charset.Charset
import kotlin.test.assertEquals

class ParseSpecs : Spek() { init {
    given("v1 xml") {
        val byteInputStream = """<RESPONSE><RESULT><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:37:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2652</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation>Cst</ToLocation><ToLocation>Mr</ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:38:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2751</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation>Söc</ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:45:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2251</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>38</ProductInformation><ToLocation>Tu</ToLocation><EstimatedTimeAtLocation>2016-01-07T17:58:00</EstimatedTimeAtLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:45:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2254</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>38</ProductInformation><ToLocation>Cst</ToLocation><ToLocation>U</ToLocation></TrainAnnouncement></RESULT></RESPONSE>""".byteInputStream(Charset.forName("UTF-8"))
        on("parse") {
            val value: List<Map<String?, String?>> = parse(byteInputStream)
            it("has list of maps") {
                assertEquals(4, value.size)
                val first = value.first()
                assertEquals("2016-01-07T17:37:00", first["AdvertisedTimeAtLocation"])
                assertEquals("2652", first["AdvertisedTrainIdent"])
                assertEquals("Mr", first["ToLocation"])
            }
        }
    }

    given("v1,1 xml") {
        val byteInputStream = """<RESPONSE><RESULT><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-23T08:52:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2718</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation><LocationName>Mr</LocationName><Priority>1</Priority><Order>0</Order></ToLocation><TimeAtLocation>2016-01-23T08:52:00</TimeAtLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-23T09:07:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2618</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation><LocationName>Mr</LocationName><Priority>1</Priority><Order>0</Order></ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-23T09:08:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2717</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation><LocationName>Söc</LocationName><Priority>1</Priority><Order>0</Order></ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-23T09:22:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2720</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation><LocationName>Mr</LocationName><Priority>1</Priority><Order>0</Order></ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-23T09:37:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2620</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation><LocationName>Mr</LocationName><Priority>1</Priority><Order>0</Order></ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-23T09:38:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2719</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation><LocationName>Söc</LocationName><Priority>1</Priority><Order>0</Order></ToLocation></TrainAnnouncement></RESULT></RESPONSE>
""".byteInputStream(Charset.forName("UTF-8"))
        on("parse") {
            val value: List<Map<String?, String?>> = parse(byteInputStream)
            it("has list of maps") {
                assertEquals(6, value.size)
                val first = value.first()
                assertEquals("2016-01-23T08:52:00", first["AdvertisedTimeAtLocation"])
                assertEquals("2718", first["AdvertisedTrainIdent"])
                assertEquals("Mr", first["ToLocation"])
            }
        }
    }
}
}

class formatSpecs : Spek() { init {
    given("date-time string") {
        val unformatted = "2016-01-13T07:37:06"
        on("formatTime") {
            val value: String = formatTime(unformatted)
            it("should") {
                assertEquals("07:37:06", value)
            }
        }
    }

    given("date-time string without seconds") {
        val unformatted = "2016-01-13T07:37:00"
        on("formatTime") {
            val value: String = formatTime(unformatted)
            it("should") {
                assertEquals("07:37", value)
            }
        }
    }

    given("time without date") {
        val unformatted = "17:37:01"
        on("formatTime") {
            val value: String = formatTime(unformatted)
            it("should") {
                assertEquals("17:37:01", value)
            }
        }
    }

    given("null") {
        val unformatted = null
        on("formatTime") {
            val value: String = formatTime(unformatted)
            it("returns empty string") {
                assertEquals("", value)
            }
        }
    }
}
}