import org.jetbrains.spek.api.Spek
import java.nio.charset.Charset
import kotlin.test.assertEquals

class ParseSpecs : Spek() { init {

    given("xml") {
        val byteInputStream = """<RESPONSE><RESULT><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:37:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2652</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation>Cst</ToLocation><ToLocation>Mr</ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:38:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2751</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>36</ProductInformation><ToLocation>Söc</ToLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:45:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2251</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>38</ProductInformation><ToLocation>Tu</ToLocation><EstimatedTimeAtLocation>2016-01-07T17:58:00</EstimatedTimeAtLocation></TrainAnnouncement><TrainAnnouncement><ActivityType>Avgang</ActivityType><AdvertisedTimeAtLocation>2016-01-07T17:45:00</AdvertisedTimeAtLocation><AdvertisedTrainIdent>2254</AdvertisedTrainIdent><LocationSignature>Tul</LocationSignature><ProductInformation>Pendeltåg</ProductInformation><ProductInformation>38</ProductInformation><ToLocation>Cst</ToLocation><ToLocation>U</ToLocation></TrainAnnouncement></RESULT></RESPONSE>""".byteInputStream(Charset.forName("UTF-8"))
        on("parse") {
            val value: List<Map<String?, String?>> = parse(byteInputStream)
            it("has list of maps") {
                assertEquals(4, value.size)
                val first = value.first()
                assertEquals("2016-01-07T17:37:00", first["AdvertisedTimeAtLocation"])
                assertEquals("2652", first["AdvertisedTrainIdent"])
            }
        }
    }
}
}