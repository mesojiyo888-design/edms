(function(global) {
    function FileComponent(wrapper) {
        this.$wrapper = $(wrapper);
        this._fileIdElementId = '';
        this._existingListElementId = '';
        this._containerId = '';
        this._inputParamName = this.$wrapper.data('param-name') || 'files';
        this._deleteSeqsQueue = []; // 삭제할 시퀀스 보관
        this._fileIndex = 0;
    }

    FileComponent.prototype = {
        autoBuild: function () {
            const paramName = this._inputParamName;
            const initialFileId = this.$wrapper.data('file-id') || '';
            const randomId = Math.random().toString(36).substring(2, 7).toUpperCase();

            this._fileIdElementId = 'hidden_' + paramName + '_' + randomId;
            this._existingListElementId = 'ul_' + paramName + '_' + randomId;
            this._containerId = 'box_' + paramName + '_' + randomId;

            const templateHtml =
                '<div style="margin-bottom:20px; padding:15px; border:1px solid #dee2e6; border-radius:6px; background:#fff;">' +
                '   <input type="hidden" id="' + this._fileIdElementId + '" name="' + paramName + 'Id" value="' + initialFileId + '" />' +
                '   <div id="' + this._containerId + '" style="display:none;"></div>' +
                '   <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">' +
                '       <span style="font-weight:bold;">📂 파일 관리 (' + paramName + ')</span>' +
                '       <button type="button" class="btn-file-add" style="background:#28a745; color:#fff; border:none; padding:6px 12px; cursor:pointer;">+ 추가</button>' +
                '   </div>' +
                '   <ul id="' + this._existingListElementId + '" style="list-style:none; padding:0; margin:0;"></ul>' +
                '</div>';

            this.$wrapper.html(templateHtml);
            const self = this;
            this.$wrapper.find('.btn-file-add').on('click', function() { self.addNewFileInput(); });
            if (initialFileId) this.loadExistingFiles(initialFileId);
            this.bindContainerChangeEvent();

            this.$wrapper.data('component', this);
        },

        loadExistingFiles: function (fileId) {
            const self = this;
            $.ajax({
                url: _CONTEXT_PATH + '/file/list',
                data: { fileId: fileId },
                success: function (list) {
                    const ul = $('#' + self._existingListElementId);
                    $.each(list, function (idx, item) {
                        const rowId = "row_" + self._inputParamName + "_" + item.fileSeq;

                        const downloadUrl = _CONTEXT_PATH + '/file/download/' + item.fileId + '/' + item.fileSeq;

                        ul.append('<li id="'+rowId+'" style="display:flex; justify-content:space-between; padding:5px;">' +
                                  '<span>' +
                                    '<a href="'+downloadUrl+'" style="text-decoration:none; color:#007bff; margin-right:10px;">💾' +
                                    item.orgFileName +
                                    '</a>' +
                                  '</span>' +
                                  '<button type="button" class="btn-remove" data-seq="'+item.fileSeq+'" style="background:#dc3545; color:#fff; border:none; cursor:pointer;">삭제</button></li>');
                    });
                    ul.find('.btn-remove').on('click', function() {
                        self._deleteSeqsQueue.push($(this).data('seq'));
                        $(this).closest('li').remove();
                    });
                }
            });
        },

        addNewFileInput: function () {
            this._fileIndex++;
            const id = this._inputParamName + "_input_" + this._fileIndex;
            $('#' + this._containerId).append('<input type="file" id="'+id+'" class="dynamic-input" name="'+this._inputParamName+'" style="display:none;" />');
            document.getElementById(id).click();
        },

        bindContainerChangeEvent: function () {
            const self = this;
            $('#' + this._containerId).on('change', '.dynamic-input', function () {
                if (this.files.length > 0) {
                    const id = $(this).attr('id');
                    const file = this.files[0];
                    // Blob URL 생성 (다운로드용)
                    const blobUrl = URL.createObjectURL(file);

                    $('#' + self._existingListElementId).append(
                        '<li id="list_'+id+'" style="display:flex; justify-content:space-between; padding:5px;">' +
                        '<span>' +
                            '<a href="'+blobUrl+'" download="'+file.name+'" style="text-decoration:none; color:#28a745; margin-right:10px;">💾' +
                            file.name +
                            '</a>' +
                        '</span>' +
                        '<button type="button" onclick="CommonFile.removeNew(\''+id+'\', \'list_'+id+'\')">❌</button>' +
                        '</li>'
                    );
                }
            });
        }
    };

    global.CommonFile = {
        createAndAppend: function(parentSelector, paramName, fileId) {
            const $wrapper = $('<div class="file-container"></div>').attr({'data-param-name': paramName, 'data-file-id': fileId});
            $(parentSelector).append($wrapper);
            new FileComponent($wrapper).autoBuild();
        },
        // [중요] ID 영역만 직접 뒤져서 수집
        appendDataFromArea: function(areaSelector, formData) {
            const $area = $(areaSelector);

            // 1. ID 값 (Hidden)
            const idInput = $area.find('input[type="hidden"]');
            formData.append(idInput.attr('name'), idInput.val());

            // 2. 삭제 시퀀스 인스턴스에서 큐 꺼내기
            const component = $area.find('.file-container').data('component');
            if (component && component._deleteSeqsQueue.length > 0) {
                component._deleteSeqsQueue.forEach(function(seq) {
                    formData.append(component._inputParamName + 'DeleteSeqs', seq);
                });
            }

            // 3. 파일들
            $area.find('.dynamic-input').each(function() {
                if(this.files.length > 0) formData.append(this.name, this.files[0]);
            });
        },
        removeNew: function(inputId, listId) {
            // 생성된 Blob URL 해제
            const aTag = $('#' + listId).find('a');
            if (aTag.length > 0) {
                URL.revokeObjectURL(aTag.attr('href'));
            }

            $('#'+inputId).remove();
            $('#'+listId).remove();

        }
    };
})(window);