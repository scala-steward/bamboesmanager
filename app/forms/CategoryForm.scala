package forms

import play.api.data.Form
import play.api.data.Forms._

object CategoryForm {

  val form: Form[Data] = Form(
    mapping(
      "name" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String)

}
