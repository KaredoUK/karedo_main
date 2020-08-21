package util

import org.specs2.mock.Mockito
import parallelai.wallet.persistence.{UserAccountDAO, ClientApplicationDAO, BrandDAO}

object TestMockPersistenceApp extends App with Mockito {
  val mockedBrandDAO = mock[BrandDAO]
  val mockedClientApplicationDAO = mock[ClientApplicationDAO]
  val mockedUserAccountDAO = mock[UserAccountDAO]

  val mockedServer = new RestServiceWithMockPersistence(8080, mockedBrandDAO, mockedClientApplicationDAO, mockedUserAccountDAO)
}
