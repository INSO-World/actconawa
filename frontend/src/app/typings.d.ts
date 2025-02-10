declare module 'cytoscape-expand-collapse' {
  const expandCollapse: cytoscape.ExpandCollapse;
  export default expandCollapse;
}

declare namespace cytoscape {
  interface Core {
    expandCollapse: (options?: ExpandCollapseOptions) => ExpandCollapseAPI;
  }

  interface ExpandCollapseOptions {
    layoutBy?: any, // for rearrange after expand/collapse. It's just layout options or whole layout function. Choose
                    // your side!
    fisheye?: boolean, // whether to perform fisheye view after expand/collapse you can specify a function too
    animate?: boolean, // whether to animate on drawing changes you can specify a function too
    animationDuration?: number, // when animate is true, the duration in milliseconds of the animation
    ready?: function, // callback when expand/collapse initialized
    undoable?: boolean, // and if undoRedoExtension exists,

    cueEnabled?: boolean, // Whether cues are enabled
    expandCollapseCuePosition?: string, // default cue position is top left you can specify a function per node too
    expandCollapseCueSize?: number, // size of expand-collapse cue
    expandCollapseCueLineSize?: number, // size of lines used for drawing plus-minus icons
    expandCueImage?: string, // image of expand icon if undefined draw regular expand cue
    collapseCueImage?: string, // image of collapse icon if undefined draw regular collapse cue
    expandCollapseCueSensitivity?: number, // sensitivity of expand-collapse cues

    edgeTypeInfo?: string, //the name of the field that has the edge type, retrieved from edge.data(), can be a function
    groupEdgesOfSameTypeOnCollapse?: boolean,
    allowNestedEdgeCollapse?: boolean,
    zIndex?: number // z-index value of the canvas in which cue ımages are drawn
  }

  interface ExpandCollapseAPI {
    collapse: (eles: cytoscape.Collection) => void;
    collapseRecursively: (eles: cytoscape.Collection, options?: any) => void;
    collapseAll: () => void;
    expand: (eles: cytoscape.Collection, options?: any) => void;
    expandRecursively: (eles: cytoscape.Collection, options?: any) => void;
    expandAll: () => void;
    clearVisualCue: () => void;
    enableCue: () => void;
    disableCue: () => void;

    isExpandable: (ele: cytoscape.SingularElement) => boolean;
    isCollapsible: (ele: cytoscape.SingularElement) => boolean;
    expandableNodes: (ele: cytoscape.SingularElement) => cytoscape.Collection;
    collapsibleNodes: (ele: cytoscape.SingularElement) => cytoscape.Collection;

    collapseEdges: (eles: cytoscape.Collection, options?: any) => void;
    expandEdges: (eles: cytoscape.Collection) => void;
    collapseEdgesBetweenNodes: (eles: cytoscape.Collection, options?: any) => void;
    expandEdgesBetweenNodes: (eles: cytoscape.Collection) => void;
    collapseAllEdges: (options?: any) => void;
    expandAllEdges: () => void;

    getCollapsedChildren: (ele: cytoscape.SingularElement) => cytoscape.Collection;
    getParent: (id: string) => cytoscape.SingularElement;
  }

}
