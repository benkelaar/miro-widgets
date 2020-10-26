# miro-widgets
Widget library for JFuture 2020 Miro coding challenge

[Coding challenge described here](https://www.notion.so/Miro-Coding-Challenge-bfcad276c07247879f34f0182407b318)

## Main approach
This solution relies on using a ConcurrentSkipListMap keyed in zIndex, with a small recursive 
function to update upstream Z-indexes.  This means that updates to widgets are transactional,
but updates over all widgets are not.  While a Z-index shift is going on concurrent reads might
get a view that misses one or two widgets.  Everything should at least be eventually consistent.

## Big-O
Solution should be log(n) for most operations.

## Further work
First next step would be to introduce concurrency testing to verify that the solution is indeed
as resilient against concurrent issues as intended.

Another fun area of further exploration can be looking in to transactional changes over all widget
Z-indexes.  This would probably require some form of explicit locking, perhaps limited in impact by
locking connected ranges.  Could potentially be made performant by introducing some form of range-indexed
data structure. 
 