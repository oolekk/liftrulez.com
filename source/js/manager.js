(function(){
  var prefxId, prefxClass, isChecked, getSide, flipChecked, initMasterCheck, initMasterFlip, checkedDirs, checkedFiles, checkedFods, addSideClasses, mkSideIds, i$, ref$, len$, side;
  prefxId = function(pre, post){
    return '#' + pre + post;
  };
  prefxClass = function(pre, post){
    return '.' + pre + post;
  };
  isChecked = function($elem){
    var checkBool;
    return checkBool = $elem.attr('checked') != null ? true : false;
  };
  getSide = function($elem){
    if ($elem.parents().filter('#leftPane').size() > 0) {
      return 'left';
    } else {
      return 'right';
    }
  };
  flipChecked = function($elem){
    if (isChecked($elem)) {
      $elem.removeAttr('checked');
    } else {
      $elem.attr('checked', true);
    }
  };
  initMasterCheck = function(side){
    $(prefxId(side, 'MasterCheck')).click(function(){
      var tgts;
      tgts = $(prefxId(side, 'Pane .check'));
      if (isChecked($(this))) {
        tgts.attr('checked', true);
      } else {
        tgts.removeAttr('checked');
      }
    });
  };
  initMasterFlip = function(side){
    $(prefxId(side, 'MasterFlip')).click(function(){
      var tgts, i$, len$, tgt;
      tgts = $(prefxId(side, 'Pane .check'));
      for (i$ = 0, len$ = tgts.length; i$ < len$; ++i$) {
        tgt = tgts[i$];
        flipChecked($(tgt));
      }
    });
  };
  document.newFodMsg = function(evt){
    var path;
    path = $(evt.target).siblings().filter('.newFodName').attr('value');
    debug.log(path);
    return {
      "path": path != null ? path : ""
    };
  };
  checkedDirs = function(side){
    return $(prefxId(side, 'Pane .dirRow')).find(':checked').siblings('.dirName').map(function(){
      return $(this).text();
    });
  };
  checkedFiles = function(side){
    return $(prefxId(side, 'Pane .fileRow')).find(':checked').siblings('.fileName').map(function(){
      return $(this).text();
    });
  };
  checkedFods = function(side){
    return append(checkedDirs(side).get(), checkedFiles(side).get());
  };
  addSideClasses = function(side){
    return $(prefxId(side, 'Pane')).find('input').add('button').map(function(){
      return $(this).addClass(side);
    });
  };
  mkSideIds = function(side){
    debug.info('mkSideIds ' + side);
    $(prefxId(side, 'Pane')).find('.MasterCheck').attr('id', side + 'MasterCheck');
    return $(prefxId(side, 'Pane')).find('.MasterFlip').attr('id', side + 'MasterFlip');
  };
  for (i$ = 0, len$ = (ref$ = ['left', 'right']).length; i$ < len$; ++i$) {
    side = ref$[i$];
    mkSideIds(side);
    initMasterCheck(side);
    initMasterFlip(side);
    addSideClasses(side);
  }
  debug.info('loaded dirmanager.js');
}).call(this);
