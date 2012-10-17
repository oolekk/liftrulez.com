prefxId = (pre,post) ->
  '#'+pre+post

prefxClass = (pre,post) ->
  '.'+pre+post

isChecked = ($elem) ->
  checkBool =
    if($elem.attr('checked')?)
      true
    else
      false

getSide = ($elem) ->
  if ($elem.parents().filter('#leftPane').size() >  0)
    'left'
  else
    'right'

flipChecked = !($elem) ->
  if(isChecked($elem))
    $elem.removeAttr('checked')
  else
    $elem.attr('checked',true)

initMasterCheck = !(side) ->
  <-! $(prefxId(side,'MasterCheck')).click
  tgts = $(prefxId(side,'Pane .check'))
  if(isChecked($(this)))
    tgts .attr('checked',true)
  else
    tgts .removeAttr('checked')

initMasterFlip = !(side) ->
  <-! $(prefxId(side,'MasterFlip')).click
  tgts = $(prefxId(side,'Pane .check'))
  for tgt in tgts
    flipChecked($(tgt))

document.newFodMsg = (evt) ->
  path = $(evt.target).siblings!filter('.newFodName').attr('value')
  debug.log(path)
  "path":
    if(path?)
      path
    else
      ""

checkedDirs = (side) ->
  <- $(prefxId(side, 'Pane .dirRow')).find(':checked').siblings('.dirName').map
  $(this).text!

checkedFiles = (side) ->
  <- $(prefxId(side, 'Pane .fileRow')).find(':checked').siblings('.fileName').map
  $(this).text!

checkedFods = (side) ->
  append(checkedDirs(side)get!,checkedFiles(side).get!)

addSideClasses = (side) ->
  <- $(prefxId(side, 'Pane')).find('input').add('button').map
  $(this).addClass(side)

mkSideIds = (side) ->
  debug.info('mkSideIds '+side)
  $(prefxId(side, 'Pane')).find('.MasterCheck').attr('id',side+'MasterCheck')
  $(prefxId(side, 'Pane')).find('.MasterFlip').attr('id',side+'MasterFlip')

for side in ['left' 'right']
  mkSideIds(side)
  initMasterCheck(side)
  initMasterFlip(side)
  addSideClasses(side)

debug.info 'loaded dirmanager.js'
