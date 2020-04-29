package com.remondis.limbus.files.bundles;

import com.remondis.limbus.files.FileSystemServiceImpl;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Bundles the components required for {@link LimbusFileService} instance operation on the real file system.
 */
@LimbusBundle
@PublicComponent(requestType = LimbusFileService.class, type = FileSystemServiceImpl.class)
public class InMemoryFileSystemBundle {

}
