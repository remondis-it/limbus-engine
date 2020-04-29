open module com.remondis.limbus.vfs {
  exports com.remondis.limbus.files;
  exports com.remondis.limbus.files.vfs;
  exports com.remondis.limbus.files.bundles;

  requires com.remondis.limbus.system.api;
  requires com.remondis.limbus.utils;
  requires com.remondis.limbus.api;

  requires commons.io;

}