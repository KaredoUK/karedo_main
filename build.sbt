lazy val root = project.in(file(".")).aggregate(db,rtb,routes)

lazy val db = project.in(file("karedo_persist"))

lazy val rtb = project.in(file("karedo_rtb")) dependsOn(db)

lazy val routes = project.in(file("karedo_routes")) dependsOn(db, rtb)
