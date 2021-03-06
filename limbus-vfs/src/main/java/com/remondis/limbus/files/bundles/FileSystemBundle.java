package com.remondis.limbus.files.bundles;

import com.remondis.limbus.files.FileSystemServiceImpl;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.system.api.LimbusBundle;
import com.remondis.limbus.system.api.PublicComponent;

/**
 * Bundles the components required for {@link LimbusFileService} instance simulating a file system in RAM.
 */
@LimbusBundle
@PublicComponent(requestType = LimbusFileService.class, type = FileSystemServiceImpl.class)
public class FileSystemBundle {

}
