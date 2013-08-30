// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.view.di.viewer.handlers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ItemRecord;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.ProviderManager;
import org.talend.repository.items.importexport.ui.managers.ZipFileManager;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RoutineImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu RoutineImportHandler constructor comment.
     */
    public RoutineImportHandler() {
        super();
    }

    @Override
    protected boolean validRelativePath(IPath relativePath) {
        boolean valid = super.validRelativePath(relativePath);
        if (valid) { // ignore system items
            ERepositoryObjectType routinesType = ERepositoryObjectType.ROUTINES;
            if (routinesType != null) {
                IPath pah = relativePath.makeRelativeTo(new Path(routinesType.getFolder()));
                if (new Path(RepositoryConstants.SYSTEM_DIRECTORY).isPrefixOf(pah)) {
                    valid = false; // system items
                }
            }
        }
        return valid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler#afterImportingItemRecords(org
     * .eclipse.core.runtime.IProgressMonitor,
     * org.talend.repository.items.importexport.ui.wizard.imports.managers.ResourcesManager,
     * org.talend.repository.items.importexport.ui.wizard.imports.models.ItemRecord)
     */
    @Override
    public void afterImportingItemRecords(IProgressMonitor monitor, ResourcesManager resManager, ItemRecord selectedItemRecord) {
        // deploy routines Jar

        final Item item = selectedItemRecord.getItem();
        if (item.eClass().equals(PropertiesPackage.eINSTANCE.getRoutineItem()) && item instanceof RoutineItem) {
            RoutineItem rItem = (RoutineItem) item;
            Set<String> extRoutines = new HashSet<String>();
            for (IMPORTType type : (List<IMPORTType>) rItem.getImports()) {
                extRoutines.add(type.getMODULE());
            }
            if (resManager instanceof ProviderManager || resManager instanceof ZipFileManager) {
                deployJarToDestForArchive(resManager, extRoutines);
            } else {
                deployJarToDest(resManager, extRoutines);
            }

        }

        super.afterImportingItemRecords(monitor, resManager, selectedItemRecord);
    }

    private void deployJarToDestForArchive(final ResourcesManager manager, Set<String> extRoutines) {
        if (extRoutines.isEmpty()) {
            return;
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {
            ILibrariesService libService = (ILibrariesService) GlobalServiceRegister.getDefault().getService(
                    ILibrariesService.class);
            IPath tmpDir = new Path(System.getProperty("user.dir") + File.separatorChar + "tmpJar"); //$NON-NLS-1$  

            File dirFile = tmpDir.toFile();
            for (IPath path : manager.getPaths()) {
                String fileName = path.lastSegment();
                if (extRoutines.contains(fileName)) {
                    try {
                        InputStream is = manager.getStream(path);
                        if (!dirFile.exists()) {
                            dirFile.mkdirs();
                        }
                        File temFile = tmpDir.append(fileName).toFile();
                        if (temFile.exists()) {
                            temFile.delete();
                        }
                        byte[] b = new byte[1024];
                        int length = 0;
                        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(temFile, true));
                        while ((length = is.read(b)) != -1) {
                            fos.write(b, 0, length);
                        }
                        fos.close();
                        //
                        libService.deployLibrary(temFile.toURI().toURL());

                        temFile.delete();

                    } catch (MalformedURLException e) {
                        ExceptionHandler.process(e);
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
            dirFile.delete();
        }
    }

    private void deployJarToDest(final ResourcesManager manager, Set<String> extRoutines) {
        File file = null;
        if (extRoutines.isEmpty()) {
            return;
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {
            ILibrariesService libService = (ILibrariesService) GlobalServiceRegister.getDefault().getService(
                    ILibrariesService.class);
            for (Object element : manager.getPaths()) {
                String value = element.toString();
                file = new File(value);
                if (extRoutines.contains(file.getName())) {
                    try {
                        libService.deployLibrary(file.toURL());
                    } catch (MalformedURLException e) {
                        ExceptionHandler.process(e);
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }

            }
        }
    }
}
