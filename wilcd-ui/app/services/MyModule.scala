package services

import com.google.inject.AbstractModule

class MyModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[MessageUpdater]).to(classOf[MessageUpdaterNoop])
  }
}
