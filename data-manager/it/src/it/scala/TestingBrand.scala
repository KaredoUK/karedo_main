import org.specs2.mutable.Specification


class TestingBrand
  extends Specification
  with ItEnvironment {

  clearAll()

  sequential

  "Brands" should {

    "Able to Create brand and ads" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId,"pakkio")
      isUUID(b.toString)
      val b1=addBrand(r.sessionId,"company2")
      isUUID(b1.toString)

      val b1a1 = addAd(r.sessionId,b1,"ad company")

      val a1 = addAd(r.sessionId,b,"myad")
      isUUID(a1.toString)
      println(s"session: ${r.sessionId} brand: $b ad: $a1\n")

      val a2 = addAd(r.sessionId,b,"second ad")
      val a3 = addAd(r.sessionId,b,"third ad")

      val ads=listAds(r.sessionId,b)
      ads.size should_==(3)

      ads.filter(_.shortText=="second ad").size should_==(1)
      ads.filter(_.shortText=="4th ad").size should_==(0)

      val read2 = getAd(r.sessionId,b,a2.toString)
      read2.shortText should_==("second ad")

      val adscompany=listAds(r.sessionId,b1)
      adscompany.size should_==(1)

    }
  }
  "Associate user to brand and ads" in {
    val r = RegisterAccount
    val r2 = RegisterAccount
    val b = addBrand(r.sessionId,"C1")
    val b2 = addBrand(r.sessionId,"C2")
    val ad1 = addAd(r.sessionId,b,"AD1")
    val ad2 = addAd(r.sessionId,b,"AD2")
    val ad3 = addAd(r.sessionId,b,"AD3")
    val ad4 = addAd(r.sessionId,b,"AD4")
    val ad5 = addAd(r.sessionId,b,"AD5")

    addBrandToUser(r.sessionId, r.userId,b)
    addBrandToUser(r.sessionId, r.userId, b2)
    addBrandToUser(r2.sessionId, r2.userId,b2)

    val brands=listBrandsForUser(r.sessionId, r.userId)
    brands.size should_==(2)

    val brandsUser2=listBrandsForUser(r2.sessionId,r2.userId)
    brandsUser2.size should_==(1)

    val suggested=getSuggestedAds(r.sessionId, r.userId, b, 2)

    suggested.size should_==(2)
    suggested(0).detailedText should_==("AD1")

    val suggested2=getSuggestedAds(r2.sessionId,r2.userId,b2,2)
    suggested2.size should_==(0)

    // removeABrand and recheck that only first user has one brand less connected
    removeBrandFromUser(r.sessionId,r.userId,b)

    val brandsAfter=listBrandsForUser(r.sessionId, r.userId)
    brandsAfter.size should_==(1)
    brandsAfter.map(_.id) should contain(b2)

    val brandsUser2After=listBrandsForUser(r2.sessionId,r2.userId)
    brandsUser2After.size should_==(1)


  }



}
