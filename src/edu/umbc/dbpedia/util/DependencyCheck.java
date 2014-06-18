package edu.umbc.dbpedia.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import java.io.BufferedReader;
import java.io.FileReader;


import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class DependencyCheck {
	
    HashSet<String> consideredRelations = new HashSet<String>(Arrays.asList("nsubj", "dobj", "nsubjpass", "agent", "num", "amod"));
    HashSet<String> consideredTaggs = new HashSet<String>(Arrays.asList("NN", "VB", "JJ", "CD", "PRP"));
    
    LexicalizedParser lp;
    TreebankLanguagePack tlp;
    GrammaticalStructureFactory gsf;

    double confidentMappingThreshold = 0.5;
    public static String dataPath;
    public static String dataPath1;
    

	public DependencyCheck() {
		// TODO Auto-generated constructor stub
        /* Read Path for model and data files */
        try{
            //InputStream input = new FileInputStream("../../../../../config.properties");
            BufferedReader br = new BufferedReader(new FileReader("../../../../../config.txt"));
            dataPath = br.readLine();
            dataPath1 = br.readLine();
            //System.out.println("Yes"+dataPath);
        }
        catch (Exception e) {
            //System.out.println("No");
        }
        /* End */

        
		System.out.println("Loading parser ...");
		//lp = LexicalizedParser.loadModel(dataPath+"/model/stanford/lexparser/englishPCFG.ser.gz", "-maxLength", "200");
        lp = LexicalizedParser.loadModel(dataPath+"/model/stanford/lexparser/englishFactored.ser.gz", "-maxLength", "200");
        
		tlp = new PennTreebankLanguagePack();
 	    gsf = tlp.grammaticalStructureFactory();

	}
	
	public Collection<TypedDependency> getDependencyCollection(String sentence){
		
        Tree parse = lp.apply(sentence);
 	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
 	    Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();

 	    return tdl;
	}
	
	public HashSet<Contradiction> getContradictions(Collection<TypedDependency>  tdl1, Collection<TypedDependency>  tdl2, ComplexPredicate firstCompound, ComplexPredicate secondCompound){
		
 	    HashSet<Contradiction> contradictionSet1 = new HashSet<Contradiction>();

 	    for( Iterator<TypedDependency> iter1 = tdl1.iterator(); iter1.hasNext(); ) {
			TypedDependency var1 = iter1.next();

			TreeGraphNode dep1 = var1.dep();
			TreeGraphNode gov1 = var1.gov();
			
			String reln = var1.reln().toString();
			
			if (consideredRelations.contains(reln)){
				
				int indexDep1 = -1;
				
				for (int i = 0; i < firstCompound.componentWords.length; i++){
					
					OrderedWord orderedWord = firstCompound.componentWords[i];
					
					if (dep1.toString().equals(orderedWord.origin + "-" + orderedWord.orderNo)){
						indexDep1 = i;
						break;
					}
				}

				int indexGov1 = -1;
				
				for (int i = 0; i < firstCompound.componentWords.length; i++){
					
					OrderedWord orderedWord = firstCompound.componentWords[i];
					
					if (gov1.toString().equals(orderedWord.origin + "-" + orderedWord.orderNo)){
						indexGov1 = i;
						break;
					}
				}
				
				
				
				if (indexDep1 >=0 && indexGov1 >= 0) {
					
					String dep_word = firstCompound.componentWords[indexDep1].tagged_word;
					String dep_tag = dep_word.substring(dep_word.lastIndexOf("_") + 1, dep_word.length());
					
					String gov_word = firstCompound.componentWords[indexGov1].tagged_word;
					String gov_tag = gov_word.substring(gov_word.lastIndexOf("_") + 1, gov_word.length());
					
					
					if (consideredTaggs.contains(dep_tag) && consideredTaggs.contains(gov_tag)){
					
						// when confident mappings are identified
						if (firstCompound.componentWords[indexDep1].getMappingValue() > confidentMappingThreshold && firstCompound.componentWords[indexGov1].getMappingValue() > confidentMappingThreshold){
							
							// collecting mappings
							HashSet<Integer[]> depMapping = new HashSet<Integer[]>();
							HashSet<Integer[]> govMapping = new HashSet<Integer[]>();
							
							for (MappingEntry entry : firstCompound.componentWords[indexDep1].potentialMappings){
								
								Integer[] mapping = new Integer[2];
								mapping[0] = indexDep1;
								mapping[1] = entry.no;
								depMapping.add(mapping);
							}
							
							for (MappingEntry entry : firstCompound.componentWords[indexGov1].potentialMappings){
								
								Integer[]  mapping = new Integer[2];
								mapping[0] = indexGov1;
								mapping[1] = entry.no;
								govMapping.add(mapping);
							}

							
							for (int i = 0; i < secondCompound.componentWords.length; i++){
								
								OrderedWord orderedWord = secondCompound.componentWords[i];
								
								if (orderedWord.getMappingNo() == indexDep1 && i != firstCompound.componentWords[indexDep1].getMappingNo()){
								
									if (orderedWord.getMappingValue() > confidentMappingThreshold){
										
										Integer[] mapping = new Integer[2];
										mapping[0] = indexDep1;
										mapping[1] = i;
										depMapping.add(mapping);
									}
								}
								
								if (orderedWord.getMappingNo() == indexGov1 && i != firstCompound.componentWords[indexGov1].getMappingNo()){
									
									if (orderedWord.getMappingValue() > confidentMappingThreshold){
										
										Integer[] mapping = new Integer[2];
										mapping[0] = indexGov1;
										mapping[1] = i;
										govMapping.add(mapping);
									}
								}
							}

							// check if one of the mapped terms also holds the same relation.
							boolean sameRelationFound = false;
							
							HashSet<String> relationsInsecondCompound = new HashSet<String>();
							HashMap<String, String> replaceSetforPrepOf = new HashMap<String, String>();
							
							
				 	    	for( Iterator<TypedDependency> iter2 = tdl2.iterator(); iter2.hasNext(); ) {
								TypedDependency var2 = iter2.next();
																
								String name = var2.reln().toString();
								
								if (name.equals("prep_of")){

									replaceSetforPrepOf.put(var2.gov().toString(), var2.dep().toString());									
								}
								
				 	    	}

							
				 	    	for( Iterator<TypedDependency> iter2 = tdl2.iterator(); iter2.hasNext(); ) {
								TypedDependency var2 = iter2.next();
								relationsInsecondCompound.add(var2.toString());
								
								String dependent = var2.dep().toString();
								String replace = replaceSetforPrepOf.get(dependent);
								
								if (replace != null){
									relationsInsecondCompound.add(var2.toString().replace(dependent, replace));
								}
				 	    	}
							
							for (Integer[] govM : govMapping){
								
								for (Integer[] depM: depMapping){
									
									String govLabel = secondCompound.componentWords[govM[1]].origin + "-" + secondCompound.componentWords[govM[1]].orderNo;
									String depLabel = secondCompound.componentWords[depM[1]].origin + "-" + secondCompound.componentWords[depM[1]].orderNo;
									
									String relation1 = reln + "(" + govLabel + ", " + depLabel + ")";
									String relation2 = null;
									String relation3 = null;
									
									if (reln.equals("nsubj")){
										relation2 = "agent" + "(" + govLabel + ", " + depLabel + ")";
									}else if (reln.equals("agent")){
										relation2 = "nsubj" + "(" + govLabel + ", " + depLabel + ")";
									}else if (reln.equals("dobj")){
										relation2 = "nsubjpass" + "(" + govLabel + ", " + depLabel + ")";
									}else if (reln.equals("nsubjpass")){
										relation2 = "dobj" + "(" + govLabel + ", " + depLabel + ")";
									}
									
									if (reln.equals("dobj")){
										relation3 = "prep_[a-z][a-z][a-z]*" + "\\(" + govLabel + ", " + depLabel + "\\)";
									}
									
									
									
									if (relationsInsecondCompound.contains(relation1)){
										sameRelationFound = true;
										break;
									}
									
									if (relation2 != null && relationsInsecondCompound.contains(relation2)){
										sameRelationFound = true;
										break;
									}
									
									if (relation3 != null){
										
										for (String rel: relationsInsecondCompound){
											if (rel.matches(relation3)){
												sameRelationFound = true;
												break;

											}
										}
										
									}

									/*
									if (relation3 != null && relationsInsecondCompound.contains(relation3)){
										sameRelationFound = true;
										break;
									}
									*/

									
								}
								
								if (sameRelationFound)
									break;
							}
							
							if (!sameRelationFound){
								contradictionSet1.add(new Contradiction(var1.toString(), firstCompound.componentWords[indexDep1].getMappingValue() * firstCompound.componentWords[indexGov1].getMappingValue()));
							}
							
						}
						
					}
					
				}
				
			} // done with processing a reln.
			
	    } // done with all relations

		return contradictionSet1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
