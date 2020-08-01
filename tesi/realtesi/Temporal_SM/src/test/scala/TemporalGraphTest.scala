import org.scalatest.funsuite.AnyFunSuite

class TemporalGraphTest extends AnyFunSuite {

    val query = new TemporalGraph(true, 4)
    query.addEdge(0,1,1).addEdge(0,2,1).addEdge(1,3,3).addEdge(3,2,4).addEdge(1,2,8)
    val target = new TemporalGraph(true, 7)
    target.addEdge(0,2,1).addEdge(2,1,2).addEdge(2,3,2).addEdge(2,4,2).addEdge(3,5,3).addEdge(3,6,3).addEdge(5,6,6).addEdge(6,4,4).addEdge(5,0,4).addEdge(3,4,8)

    val RIProva = new RISolver(target,false)

    val query2 = new TemporalGraph(true, 4)
    query2.addEdge(1,0,2).addEdge(0,2,8).addEdge(2,1,3).addEdge(1,3,1).addEdge(3,0,4)
    val target2 = new TemporalGraph(true, 14)
    target2.addEdge(0,1,1).addEdge(0,3,2).addEdge(1,4,3).addEdge(1,3,5).addEdge(2,0,4).addEdge(3,2,10).addEdge(4,5,4).addEdge(5,6,6).addEdge(5,3,2).addEdge(6,3,4).
        addEdge(6,8,12).addEdge(6,10,9).addEdge(7,6,6).addEdge(7,9,5).addEdge(8,7,7).addEdge(9,6,9).
        addEdge(10,12,2).addEdge(11,10,3).addEdge(11,12,1).addEdge(11,13,8).addEdge(13,10,20)

    val RIProva2 = new RISolver(target2,false)

    test("test contains node 0"){
        var param=new query.Contact(1,1)
        assert(query.outAdjListTimes(0).containsKey(param.hashCode()))
        param=new query.Contact(0,1)
        assert(query.inAdjListTimes(1).containsKey(param.hashCode()))
        param=new query.Contact(2,1)
        assert(query.outAdjListTimes(0).containsKey(param.hashCode()))
        
    }

    test("RISolver 1"){
        RIProva.solve(query,5)
        assert(RIProva.getNumMatches()==1)
    }

    test("RISolver 2"){
        RIProva2.solve(query2,5)
        assert(RIProva2.getNumMatches()==2)
    }

    test("test mapping condition 1"){
        val mappings =new Array[Int](4) map(value => -1)
        assert(query.testNodesMapping(target,0,2,mappings))
    }

    test("test mapping condition 2"){
        val mappings =new Array[Int](4) map(value => -1)
        assert(query.testNodesMapping(target,1,3,mappings))
    }

    test("test mapping condition 3"){
        val mappings =new Array[Int](4) map(value => -1)
        assert(query.testNodesMapping(target,2,4,mappings))
    }

    test("test mapping condition 4"){
        val mappings =new Array[Int](4) map(value => -1)
        assert(query.testNodesMapping(target,3,6,mappings))
    }

    test("test mapping condition 5"){
        val mappings =new Array[Int](4) map(value => -1)
        assert(!query.testNodesMapping(target,0,5,mappings))
    }

    test("test mapping condition 6"){
        val mappings =new Array[Int](4) map(value => -1)
        assert(!query.testNodesMapping(target,0,6,mappings))
    }

    test("test mapping condition 7"){
        var mappings =new Array[Int](4) map(value => -1)
        assert(!query.testNodesMapping(target,2,6,mappings))
    }

    test("test mapping condition 8"){
        var mappings =new Array[Int](4) map(value => -1)
        mappings(0)=2
        assert(!query.testNodesMapping(target,2,6,mappings))
    }

    test("test mapping condition 9"){
        var mappings =new Array[Int](4) map(value => -1)
        assert(!query.testNodesMapping(target,3,5,mappings,1))
    }

    test("test mapping condition 10"){
        var mappings =new Array[Int](4) map(value => -1)
        //tests for delta condition
        assert(!query.testNodesMapping(target,1,5,mappings,5))
        assert(!query.testNodesMapping(target,2,5,mappings,5))
    }

    test("test mapping condition 11"){
        var mappings =new Array[Int](3) map(value => -1)
        //tests for delta condition simple
        val exquery = new TemporalGraph(true,3)
        val extarget = new TemporalGraph(true,3)
        exquery.addEdge(0,1,1).addEdge(1,2,8)
        extarget.addEdge(0,1,2).addEdge(1,2,3)
        assert(!exquery.testNodesMapping(extarget,0,0,mappings))
        assert(exquery.testNodesMapping(extarget,1,1,mappings))
        assert(!exquery.testNodesMapping(extarget,2,2,mappings))

    }

    test("test mapping condition 12"){
        var mappings =new Array[Int](4) map(value => -1)
        assert(query2.testNodesMapping(target2,0,3,mappings,1))
    }

    test("test mapping condition 13"){
        var mappings =new Array[Int](4) map(value => -1)
        assert(query2.testNodesMapping(target2,1,0,mappings,1))
    }

    test("test mapping condition 14"){
        var mappings =new Array[Int](4) map(value => -1)
        assert(query2.testNodesMapping(target2,2,2,mappings,1))
    }

    test("test mapping condition 15"){
        var mappings =new Array[Int](4) map(value => -1)
        assert(query2.testNodesMapping(target2,3,1,mappings,1))
    }


    test("test compatibility 1"){
        assert(query.testCompatibility(target,0,2,5))
    }

    test("test compatibility 2"){
        assert(query.testCompatibility(target,1,3,5))
    }

    test("test compatibility 3"){
        assert(query.testCompatibility(target,2,4,5))
    }

    test("test compatibility 4"){
        assert(query.testCompatibility(target,3,6,5))
    }

    test("test compatibility 5"){
        assert(!query.testCompatibility(target,1,0,5))
    }
    test("test compatibility 6"){
        assert(!query.testCompatibility(target,1,1,5))
    }
    test("test compatibility 7"){
        assert(!query.testCompatibility(target,1,4,5))
    }
}