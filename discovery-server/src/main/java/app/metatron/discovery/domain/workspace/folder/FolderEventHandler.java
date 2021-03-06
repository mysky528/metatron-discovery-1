/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.metatron.discovery.domain.workspace.folder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;

import app.metatron.discovery.domain.workspace.BookTreeService;

/**
 * Created by kyungtaak on 2016. 5. 13..
 */
@RepositoryEventHandler(Folder.class)
public class FolderEventHandler {

  @Autowired
  BookTreeService bookTreeService;
  
  @HandleBeforeCreate
  @PreAuthorize("hasPermission(#folder, 'PERM_WORKSPACE_WRITE_BOOK')")
  public void checkCreateAuthority(Folder folder) {
  }

  @HandleAfterCreate
  public void handleAfterCreate(Folder folder) {
    // Tree 생성
    bookTreeService.createTree(folder);
  }

  @HandleBeforeSave
  @PreAuthorize("hasPermission(#folder, 'PERM_WORKSPACE_WRITE_BOOK')")
  public void checkUpdateAuthority(Folder folder) {
  }

  @HandleBeforeDelete
  @PreAuthorize("hasPermission(#folder, 'PERM_WORKSPACE_WRITE_BOOK')")
  public void checkDeleteAuthority(Folder folder) {
    // Tree 삭제
    bookTreeService.deleteTree(folder);
  }
}
