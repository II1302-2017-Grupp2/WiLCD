package controllers

sealed trait Page

object Page {
  case object Index extends Page
  case object ScheduleMessage extends Page
  case object Settings extends Page
  case object SignIn extends Page
  case object SignUp extends Page
  case object SignOut extends Page
}
