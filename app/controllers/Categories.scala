package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import forms.CategoryForm
import models.daos.CategoryDAO
import models.{Category, User}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class Categories @Inject()(categoryDAO: CategoryDAO,
                           val messagesApi: MessagesApi,
                           val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator] {

  implicit val categoryWrites = Json.writes[Category]

  def categories = SecuredAction.async { implicit request =>
    categoryDAO.all.map(categories => Ok(views.html.categories(categories, request.identity)))
  }

  def add = SecuredAction.async { implicit request =>
    Future(Ok(views.html.categoryAdd(CategoryForm.form, request.identity)))
  }

  def save = SecuredAction.async { implicit request =>
    CategoryForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.categoryAdd(form, request.identity))),
      data => {
        val category = Category(UUID.randomUUID, data.name)
        for {
          _ <- categoryDAO.save(category)
          categories <- categoryDAO.all
        } yield {
          Ok(views.html.categories(categories, request.identity))
        }
      }
    )
  }

  def all = SecuredAction.async {
    categoryDAO.all.map(categories => Ok(Json.toJson(categories)))
  }

  def get(id: String) = SecuredAction.async {
    try {
      val uuid = UUID.fromString(id)
      categoryDAO.get(uuid).map {
        case Some(category) => Ok(Json.toJson(category))
        case None => NotFound(Json.toJson(Messages("category.not_found")))
      }
    } catch {
      case _: IllegalArgumentException => Future(BadRequest(Json.toJson(Messages("uuid.invalid"))))
    }
  }
}