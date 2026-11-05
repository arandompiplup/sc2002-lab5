"""Build the Stage 1 report body and merge locally rendered vector diagram PDFs.

Use the bundled Python runtime (ReportLab and pypdf). Diagram PDFs and the
captured CLI evidence live in the build directory passed with --build-dir.
"""
from pathlib import Path
from html import escape
import argparse
import io
import json
from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Preformatted
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from pypdf import PdfReader, PdfWriter

ROOT = Path(__file__).resolve().parents[2]
NAVY = colors.HexColor('#18354A')
BLUE = colors.HexColor('#28617A')
INK = colors.HexColor('#253440')
MUTED = colors.HexColor('#566977')
PALE = colors.HexColor('#EDF3F6')
LINE = colors.HexColor('#D3DEE5')
FONT_DIR = Path('/System/Library/Fonts/Supplemental')
for name, filename in [('Arial','Arial.ttf'),('Arial-Bold','Arial Bold.ttf'),('Arial-Italic','Arial Italic.ttf'),('Mono','Courier New.ttf')]:
    pdfmetrics.registerFont(TTFont(name, str(FONT_DIR / filename)))
pdfmetrics.registerFontFamily('Arial', normal='Arial', bold='Arial-Bold', italic='Arial-Italic', boldItalic='Arial-Bold')
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='TitleReport',fontName='Arial-Bold',fontSize=29,leading=33,textColor=NAVY,spaceAfter=15))
styles.add(ParagraphStyle(name='H1Report',fontName='Arial-Bold',fontSize=19,leading=24,textColor=NAVY,spaceAfter=12))
styles.add(ParagraphStyle(name='H2Report',fontName='Arial-Bold',fontSize=12.5,leading=17,textColor=BLUE,spaceBefore=11,spaceAfter=6))
styles.add(ParagraphStyle(name='BodyReport',fontName='Arial',fontSize=10,leading=14.4,textColor=INK,spaceAfter=8))
styles.add(ParagraphStyle(name='SmallReport',fontName='Arial',fontSize=8.3,leading=11.6,textColor=MUTED,spaceAfter=6))
styles.add(ParagraphStyle(name='CellReport',fontName='Arial',fontSize=8.6,leading=11.6,textColor=INK))
styles.add(ParagraphStyle(name='HeaderCellReport',fontName='Arial-Bold',fontSize=8.8,leading=12,textColor=colors.white))
styles.add(ParagraphStyle(name='CodeReport',fontName='Mono',fontSize=8.2,leading=11,textColor=INK,backColor=PALE,borderPadding=9,spaceBefore=5,spaceAfter=11))

story=[]
markdown=[]
page_titles=[]
WIDTH=A4[0]-96

def p(text, small=False):
    story.append(Paragraph(text,styles['SmallReport' if small else 'BodyReport']))
    markdown.append(text)

def h(text):
    story.append(Paragraph(text,styles['H2Report']))
    markdown.append('## '+text)

def page(title):
    if page_titles: story.append(PageBreak())
    page_titles.append(title)
    story.append(Paragraph(title,styles['H1Report']))
    markdown.append('\n# '+title+'\n')

def table(headers,rows,widths):
    def cell(value,head=False):
        return Paragraph(escape(str(value)).replace('\n','<br/>'),styles['HeaderCellReport' if head else 'CellReport'])
    data=[[cell(x,True) for x in headers]]+[[cell(x) for x in row] for row in rows]
    t=Table(data,colWidths=[WIDTH*x for x in widths],repeatRows=1,hAlign='LEFT')
    commands=[('BACKGROUND',(0,0),(-1,0),NAVY),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),7),('RIGHTPADDING',(0,0),(-1,-1),7),('TOPPADDING',(0,0),(-1,-1),6),('BOTTOMPADDING',(0,0),(-1,-1),6),('LINEBELOW',(0,0),(-1,0),0.7,NAVY)]
    for row in range(1,len(data)):
        commands += [('BACKGROUND',(0,row),(-1,row),PALE if row%2 else colors.white),('LINEBELOW',(0,row),(-1,row),0.3,LINE)]
    t.setStyle(TableStyle(commands))
    story.extend([t,Spacer(1,9)])
    markdown.append('| '+' | '.join(headers)+' |')
    markdown.append('| '+' | '.join(['---']*len(headers))+' |')
    markdown.extend('| '+' | '.join(str(x).replace('\n','; ') for x in row)+' |' for row in rows)

def code(text):
    story.append(Preformatted(text,styles['CodeReport']))
    markdown.append('```\n'+text+'\n```')

def note(label,text):
    box=Table([[Paragraph('<b>'+label+'</b><br/>'+text,styles['BodyReport'])]],colWidths=[WIDTH])
    box.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,-1),PALE),('BOX',(0,0),(-1,-1),0.5,LINE),('LEFTPADDING',(0,0),(-1,-1),10),('RIGHTPADDING',(0,0),(-1,-1),10),('TOPPADDING',(0,0),(-1,-1),9),('BOTTOMPADDING',(0,0),(-1,-1),4)]))
    story.extend([box,Spacer(1,10)])
    markdown.append('**'+label+'** '+text)


def content(evidence):
    page('Restaurant Rush')
    story.pop()
    story.append(Spacer(1,22))
    story.append(Paragraph('Restaurant Rush',styles['TitleReport']))
    p('<font size="16" color="#28617A"><b>Stage 1 design and implementation report</b></font>')
    p('SC2002 Object-Oriented Design &amp; Programming<br/>Prepared 5 November 2026',small=True)
    story.append(Spacer(1,12))
    note('Report scope','A completed report of the current Stage 1 implementation: regular customers, the fixed menu, two tables and Waiter/Chef/Cashier service. This is not a completed Stage 3 submission. The final-assignment requirements still outstanding are stated on page 11.')
    h('Design summary')
    p('The implementation separates console presentation, turn coordination and domain behaviour. Private game-owned objects enforce legal operations; immutable display views keep the CLI from mutating them. Customer and staff inheritance reuse genuine shared rules, while two active interfaces define useful dependency boundaries. [1, 5, 6]')
    h('Verified functional result')
    table(['Evidence','Observed outcome'],[
        ['Contract checks','67 domain/controller checks passed.'],
        ['Reproducible victory','4 paid customers; $51.00; paid satisfaction 80.0; all 18 turns.'],
        ['Reproducible defeat','0 paid customers; $0.00; satisfaction N/A; all 18 turns.'],
        ['Service-priority experiment','Delaying customer 3 lowers the tested run average to 76.0.'],
    ],[0.31,0.69])
    h('Reading guide')
    p('Requirements and responsibilities: pp. 2-3. Chapter 9 design discussion: pp. 4-6. Customer 3 scenario justification: p. 7. Test evidence and reproducible runs: pp. 8-9. Development analysis: p. 10. Remaining submission work: p. 11. References and diagram map: p. 12.')
    p('Appendices A-F contain the vector UML diagrams. Larger diagrams use large-format pages to retain readable labels; they can be zoomed without raster blur.',small=True)

    page('1  Requirements and implemented rules')
    p('The base setting is taken from the original Word assignment, Sections 2-4. The following table distinguishes implemented rules from later-stage requirements. [1]')
    table(['Requirement','Stage 1 implementation / evidence'],[
        ['18 turns; 2 tables','RestaurantGame owns the turn counter and exactly two Table instances. Completion is checked only after turn 18.'],
        ['Six scheduled arrivals','One RegularCustomer arrives at turns 1, 4, 7, 10, 13 and 16.'],
        ['Customers choose food','RegularCustomer chooses when placeOrder() executes; the CLI only displays the result.'],
        ['Limited staff work','One successful Waiter task, one Chef preparation unit and one automatic Cashier payment per turn.'],
        ['Waiter before Chef','TurnPhase guards prevent preparation from running before the Waiter choice.'],
        ['READY before service','Order records readyTurn and rejects service during that same turn.'],
        ['Eating then payment','A customer served at T becomes READY_TO_PAY at T+1 and can pay at T+2 at the earliest.'],
        ['Waiting and abandonment','Waiting/seated customers lose 8 points, clamped at zero. Departure cancels an unfinished order and releases the table.'],
        ['Valid accounting','Bills use integer cents. Revenue includes only collected payments. Average uses satisfaction recorded on payment.'],
        ['Robustness and offline use','Invalid choices explain failure and retain the task. No runtime network access or external Java libraries are required.'],
        ['Victory','At least 4 paid customers AND at least 4500 cents revenue AND paid average at least 60.'],
    ],[0.28,0.72])
    h('Food selection: deterministic, not unseeded random')
    p('Section 2.2 permits a documented deterministic rule or seeded randomness. This implementation documents the cycle Salad, Burger, Pasta by customer ID: IDs 1/4 choose Salad, 2/5 Burger and 3/6 Pasta. Therefore customer 3 is not the only Pasta customer. [1, 6]')

    page('2  Abstractions and responsibilities')
    p('The allocation follows the boundary/control/entity distinction from Chapters 6 and 8. A controller coordinates the use case, while each entity owns the rules for its own state. [2, 4]')
    table(['Abstraction','Responsibility and main collaborators'],[
        ['RestaurantRush\nBoundary','Collect decisions, format snapshots and recover from input/action errors. Depends on RestaurantGameInterface.'],
        ['RestaurantGame\nControl','Own customers, waiting queue, tables and staff; enforce phase order; aggregate results. Delegates lifecycle operations.'],
        ['Customer\nAbstract entity','Maintain satisfaction, customer state, eating/payment timing and associated order/table.'],
        ['RegularCustomer','Supply loss of 8 and deterministic food preference through the protected variation hooks.'],
        ['Order\nEntity','Track preparation units, readiness turn, service/payment/cancellation status and the bill in cents.'],
        ['Table\nEntity','Maintain occupancy and a consistent seating association with Customer.'],
        ['Staff\nAbstract parent','Own the one-successful-task allowance; provide final reset/availability behaviour.'],
        ['Waiter / Chef / Cashier','Perform role-specific work. Cashier chooses the earliest eligible READY_TO_PAY customer; arrival ID breaks ties.'],
        ['CustomerView / TableView','Copy display data into immutable objects, without retaining live model references.'],
        ['RestaurantGameInterface','Expose the console use cases and read-only queries, without staff resets or arbitrary entity setters.'],
        ['MenuItemInterface / MenuItem','Provide the name, price and preparation workload. The enum supplies the three immutable Stage 1 dishes.'],
    ],[0.30,0.70])
    h('Ownership and collaboration')
    p('The game creates and owns its staff, tables and customers. Each customer creates its own order. Customer and Table have a temporary association: paying or leaving releases the table, but both objects remain in game history. Menu enum instances are shared; an Order references rather than exclusively owns its item.')
    p('The waiting list is an ordered subset of the owned customer collection. It is not a second owner. Simple getters and temporary usage dependencies are omitted from the core diagram to keep the structural relationships readable. [2]')

    page('3  Encapsulation and single responsibility')
    h('Encapsulation protects valid behaviour')
    p('Private fields alone do not preserve game rules. The controller accepts IDs, finds its own entities and enforces the current phase. It does not return its live staff or mutable domain objects to the CLI. Instead, queries return lists of copied CustomerView/TableView objects. An old snapshot stays unchanged after later game actions. [5, 6]')
    p('Domain operations express intent: placeOrder(), receiveFood(), pay() and Table.seat(). There is no general setState(PAID) operation. Local guards validate the transition; the controller owns the global turn protocol. The Waiter marks its task used only after the requested domain operation succeeds.')
    h('SRP: keep eligibility with the entity')
    code('''// Customer owns the ordering precondition.
public final boolean canOrder() {
    return state == State.SEATED && order == null;
}

// The CLI asks for the result instead of defining the rule.
game.eligibleFor(WaiterTask.ORDER);''')
    p('Changing the ordering precondition belongs in Customer; changing console formatting belongs in RestaurantRush. RestaurantGame remains a coordinator, not a class that directly assigns every entity field. This is a cohesive responsibility split, rather than a rule that each class must contain only one method. [5]')
    table(['Phase','Only accepted progression'],[
        ['BETWEEN_TURNS','startTurn() increments the internal counter, resets staff, adds a scheduled arrival.'],
        ['WAITER','One successful seat/order/serve command, or waitWaiter(), moves to CHEF.'],
        ['CHEF','One successful prepare command, or waitChef(), moves to END_OF_TURN.'],
        ['END_OF_TURN','finishTurn() performs eating, payment and waiting/departure processing.'],
        ['FINISHED','Reached after turn 18; victory() may now evaluate the targets.'],
    ],[0.27,0.73])
    p('Rejected commands leave the current phase and task availability unchanged. A caller cannot reset a turn by supplying a repeated turn number, because startTurn() takes no argument. The contract tests exercise these behaviours directly, without using the CLI.',small=True)

    page('4  Open-closed and substitution principles')
    h('OCP: vary preferences and loss, retain the lifecycle')
    p('Customer is an abstract base with final lifecycle operations. RegularCustomer supplies satisfactionLoss() and chooseMenuItem(). The shared end-of-turn algorithm calls the loss hook through a Customer reference; the controller does not inspect customer subtypes. [5, 6]')
    code('''// Shared algorithm in Customer:
satisfaction = Math.max(0, satisfaction - satisfactionLoss());

// RegularCustomer supplies its specific behaviour:
@Override
protected int satisfactionLoss() { return 8; }''')
    p('A future customer subtype can change the hook implementation without rewriting the turn loop. The arrival setup will still need to register/select the new types; OCP does not mean that no source file may ever change. Stage 1 has only one production customer subtype, so this is an extension point rather than a completed multi-type demonstration.')
    h('LSP: preserve the shared behavioural contracts')
    table(['Parent contract','How the current design preserves it'],[
        ['Customer lifecycle','RegularCustomer uses the same seating, ordering, eating, payment and departure sequence. Final lifecycle methods prevent replacement of that protocol.'],
        ['Customer variation hooks','satisfactionLoss() must be positive; chooseMenuItem() must return a non-null valid item. Each future subtype must be reviewed and tested against these preconditions.'],
        ['Staff allowance','All three roles share final beginTurn()/available() behaviour. Successful role-specific work consumes one task; rejected work does not.'],
        ['Menu abstraction','A valid immutable item has a positive price and preparation workload. Order can prepare and bill another implementation without requiring the fixed enum.'],
    ],[0.28,0.72])
    h('Limits of the evidence')
    p('The tests check all current staff roles and a test-only alternate menu implementation. They do not prove correctness for future VIP/Critic types. A subtype could still violate a documented hook contract; inheritance syntax alone is not evidence of LSP. No VIP, Critic or combo behaviour is included in the running Stage 1 game. [1, 5]')

    page('5  Interface segregation and dependency inversion')
    h('ISP: interfaces serve actual clients')
    p('RestaurantGameInterface groups the queries and commands needed by the current console. It does not expose Staff.beginTurn() or entity mutation setters. MenuItemInterface provides only a name, price and workload. Neither interface exists merely to duplicate every method of a concrete class. [5]')
    p('The Staff parent contains only the common allowance. It does not require the Chef to implement seating or the Waiter to implement payment. If a future display-only client needs fewer game operations, a smaller read-only interface may become useful; it is not necessary to add it speculatively.')
    note('Current-source qualification','Four unused declarations remain in the checkout: WaiterInterface, ChefInterface, CashierInterface and TableInterface. Their corresponding classes do not currently implement them. They are not evidence of realised polymorphism or DIP, and the runtime diagrams deliberately do not invent realisation arrows for them.')
    h('DIP: inject a useful abstraction at the boundary')
    code('''private final RestaurantGameInterface game;

public RestaurantRush(RestaurantGameInterface game,
                      Scanner input, PrintStream output) {
    this.game = Objects.requireNonNull(game);
    // Input and output are also supplied by the caller.
}''')
    p('Only main() selects the concrete RestaurantGame and supplies it to the console. This composition root allows a different implementation to be supplied without rewriting the console. Order similarly depends on MenuItemInterface rather than the MenuItem enum. Constructor injection is a technique for dependency inversion; no framework is required. [5, 6]')
    h('Proportional application of the principles')
    p('RestaurantGame intentionally owns concrete staff and tables, which have no alternate implementation in Stage 1. Not every concrete dependency is a design fault. The chosen abstractions isolate genuine boundaries and expected variations while keeping the introductory implementation understandable.')
    p('Terminology: Chapter 9 labels DIP "Dependency Injection Principle"; its standard name is Dependency Inversion Principle. The report uses the standard name while applying the dependency rule expressed in the lecture.',small=True)

    page('6  Why the sequence follows customer 3')
    p('<b>Customer 3 was selected because the documented ordering cycle makes this customer order Pasta, the only Stage 1 dish needing two preparation units.</b> This lets one trace show partial preparation, completion to READY, next-turn service, a full eating turn, payment and table release. [1, 6]')
    table(['Reason','Justification'],[
        ['Exercise the longest preparation path','Salad/Burger finish in one unit. Pasta explicitly shows PREPARING after unit 1 and READY only after unit 2.'],
        ['Make staff sequencing visible','The Waiter acts before the Chef; the second unit completed on turn 9 cannot be served until turn 10.'],
        ['Use a reproducible scenario','The preference rule fixes customer 3\'s food. A marker can reproduce it without relying on an unseeded random outcome.'],
        ['Expose a manager trade-off','Prioritising service on turn 10 preserves satisfaction 76. Waiting one turn lowers it to 68 and postpones payment.'],
    ],[0.31,0.69])
    table(['Turn','Customer 3 event','Satisfaction'],[
        ['7','Arrives at 100; Waiter seats at table 1.','92'],
        ['8','Customer chooses Pasta; Chef performs unit 1/2.','84'],
        ['9','Chef performs unit 2/2; order becomes READY.','76'],
        ['10','Waiter serves; customer becomes EATING.','76'],
        ['11','One full eating turn completes: READY_TO_PAY.','76'],
        ['12','Cashier collects $18.00; customer/order PAID; table free.','76 recorded'],
    ],[0.10,0.73,0.17])
    h('This does not skip the beginning of the game')
    p('The program still executes all 18 turns. In the victory plan, customer 1 arrives on turn 1, orders Salad and pays on turn 5; customer 2 arrives on turn 4, orders Burger and pays on turn 8. The focused trace begins with customer 3\'s arrival on turn 7, not with the order on turn 8.')
    p('The Word brief\'s Section 3.3 illustrates a generic Pasta customer arriving on turn 1. That example explains timing; it does not require customer ID 1 to choose Pasta. Starting with customer 1 would also be valid, but would show a one-unit Salad order under the current documented preference rule. Customer 6 also chooses Pasta, but arrives too late to pay by turn 18. [1]')

    page('7  Functional verification and observed results')
    p('Evidence was rerun on 5 November 2026 using Java 17 and clean temporary compilation directories. The existing regression runner compiles with javac -Xlint:all, executes 67 contract checks and runs the CLI victory, defeat and input-recovery scenarios. [6]')
    code('python3 tests/stage1_smoke.py')
    code('''PASS: 67 Stage 1 domain/controller contract checks.
PASS: Stage 1 victory, defeat, unavailable tasks,
      cancellation, and invalid input.''')
    table(['Test area','Observed contract'],[
        ['Turn protocol','No repeated startTurn(), no Chef-before-Waiter, no extra Waiter task, no turn 19.'],
        ['Encapsulation','Old customer and nested table snapshots remain unchanged; returned lists reject structural mutation.'],
        ['Rejected actions','Invalid seating/order/preparation preserves phase, task availability and existing associations.'],
        ['Preparation and eating','Two-unit Pasta; no same-turn service; no eating completion on the service turn; payment waits another turn.'],
        ['Accounting','No payment before eligibility or twice for the same customer; payment releases the table and records satisfaction.'],
        ['Abandonment','8-point loss, zero clamp, cancel PREPARING and READY orders, release table; no abandoned revenue.'],
        ['Cashier','Earliest READY_TO_PAY priority, arrival-ID tie-break and at most one payment per turn.'],
        ['Substitution and boundaries','All Staff reset contracts; alternate menu implementation; turn-16 arrival cannot pay by turn 18.'],
        ['CLI robustness','Non-numeric values, nonexistent IDs, unavailable tasks and selector cancellation permit continued play.'],
    ],[0.25,0.75])
    h('Independent full-run captures')
    table(['Run','Paid','Revenue','Paid average','Result'],[
        ['Victory plan','4','$51.00','80.0','Victory'],
        ['Wait throughout','0','$0.00','N/A','Defeat'],
        ['Delayed-service plan','4','$51.00','76.0','Victory'],
    ],[0.31,0.10,0.16,0.21,0.22])
    p('All three captured runs completed 18 turns with no stderr output. These results are specific executable evidence, not a proof of every future extension.',small=True)

    page('8  Reproducible manager choices')
    p('The seat command\'s first prompt is queue position, not customer ID. In the plan below the named customer is always at queue position 1 when seated. Chef actions identify the customer whose order is prepared. [6]')
    table(['Turn','Waiter choice','Chef choice'],[
        ['1','Seat customer 1 at table 1','Wait'],
        ['2','Take customer 1\'s order','Prepare customer 1'],
        ['3','Serve customer 1','Wait'],
        ['4','Seat customer 2 at table 2','Wait'],
        ['5','Take customer 2\'s order','Prepare customer 2'],
        ['6','Serve customer 2','Wait'],
        ['7','Seat customer 3 at table 1','Wait'],
        ['8','Take customer 3\'s order','Prepare customer 3'],
        ['9','Wait','Prepare customer 3'],
        ['10','Serve customer 3','Wait'],
        ['11','Seat customer 4 at table 2','Wait'],
        ['12','Take customer 4\'s order','Prepare customer 4'],
        ['13','Serve customer 4','Wait'],
        ['14-18','Wait','Wait'],
    ],[0.12,0.53,0.35])
    h('Defeat run')
    p('Choose Wait for both staff on every turn: 36 zero inputs. No customer pays; the final display is Defeat, $0.00, paid average N/A and two unhappy departures after 18 turns.')
    h('Measured effect of delaying customer 3')
    p('Change turn 10 to Wait, serve customer 3 on turn 11, seat customer 4 on turn 12, order/prepare customer 4 on turn 13 and serve on turn 14. Keep the other actions unchanged. The paid count and revenue remain 4 and $51.00, but average satisfaction falls from 80.0 to 76.0. The run still wins; the experiment demonstrates a measurable outcome change, not an unsupported claim that every delay causes defeat.')

    page('9  Design changes, alternatives and reflection')
    table(['Earlier structure','Current structure and justification'],[
        ['CLI eligibility lambdas','Entity canOrder()/canPrepare()/canBeServed() queries; controller supplies eligible snapshots. One rule definition.'],
        ['Caller-controlled turn numbers','Internal turn counter and guarded phases. The UI cannot restart a turn or rearrange task order.'],
        ['Live staff/entity getters','Private owned objects plus immutable copied views. Reduces accidental state mutation through the boundary.'],
        ['Consume task before action','Validate/perform action first, then record success. Invalid choices retain staff work.'],
        ['Final concrete customer','Abstract Customer and RegularCustomer, with narrow behaviour hooks rather than copied lifecycle code.'],
        ['Concrete enum inside Order','MenuItemInterface provides a small item contract. Existing preparation works with another valid implementation.'],
        ['Stale folder/build assumptions','Actual packages and recursive test source discovery. A single console implementation is used.'],
    ],[0.30,0.70])
    h('Trade-offs and one alternative')
    p('Immutable views duplicate some fields and require copying. For at most six customers this cost is small, and the separation is easy to explain and test. Keeping one shared Staff allowance avoids duplicated flags, but the parent deliberately does not become a universal interface containing unrelated role tasks.')
    p('A composition alternative is to supply Customer with a CustomerBehaviour strategy for loss and food preference. That would allow behaviour replacement without subclassing. The current small inheritance hierarchy was chosen because the variations form a coherent customer family and fit the course\'s introductory overriding examples. A strategy remains a valid alternative, not an implemented feature.')
    h('Engineering reflection based on the code')
    p('The important improvement is enforcing behavioural contracts at the domain/control boundary. Moving a condition out of the CLI is useful only when the action method also enforces it. Similarly, an interface improves dependency management only when callers actually use that abstraction. Final lifecycle methods, phase guards and clean-build tests provide concrete evidence for these decisions. [5, 6]')
    p('This is an engineering analysis of the implementation, not a fabricated account of individual members\' work or learning. Member contributions and personal reflections must come from the group.',small=True)

    page('10  Stage scope and remaining submission work')
    p('The Word assignment submits three independently runnable stages together. This report completes the design/evidence narrative for the current Stage 1; it cannot truthfully certify the final submission while later features and group-owned evidence are absent. [1]')
    table(['Submission requirement','Verified current position / next action'],[
        ['Stage 1 game','Implemented and verified by the tests and full-run captures in this report.'],
        ['Runnable stage tags','Current branch is named stage-1, but no local Git tags were present when checked. A branch name does not satisfy the tag requirement.'],
        ['Stage 2 customers','VIP/Critic behaviour is not implemented. Add reproducible arrivals, polymorphic behaviour and regression evidence.'],
        ['Stage 3 features','Host, ComboMeal and Happy Hour are not implemented. Add their rules and order-time pricing evidence.'],
        ['Final class diagram','Appendices A-B describe Stage 1. Update to match the eventual Stage 3 source and enums.'],
        ['Final sequence scenario','Appendices C-F show Stage 1. The final scenario must additionally contain a Stage 2/3 behaviour.'],
        ['Stage-transition evidence','Provide actual stage-1/2/3 tags and compare links, with new/modified/deleted files and reasons for both transitions.'],
        ['Group video / README','Record the required group demonstration and add accessible video details and timestamps. These have not been invented.'],
        ['Member identity / reflection','Add the group\'s confirmed identification and authentic contribution/reflection material. No student names or contributions are assumed here.'],
        ['Declarations','Complete the course\'s signed Original Work and individual GAI-assistance declarations. This report is not a signature or declaration.'],
    ],[0.29,0.71])
    h('Repository state recorded for this report')
    p('Repository: <link href="https://github.com/arandompiplup/sc2002-lab5">github.com/arandompiplup/sc2002-lab5</link><br/>Inspected branch: stage-1. Inspected HEAD: 86c3aea (Remove old RestaurantRush). The tested code is the local working checkout; the report and updated diagram exports may still be uncommitted.')
    p('The brief specifies 15 November 2026 at 11:59 pm as the group submission deadline. The remaining work above is a scope record, not evidence that those items have been completed. [1]',small=True)

    page('11  References and diagram map')
    h('Sources')
    p('<b>[1] Assignment brief.</b> SC2002_Restaurant_Rush_Group_Assignment.docx. Sections 2.1-2.2 (base setting and food choice), 3 (turn rules/example), 4.1-4.4 (stages/design freedom), 5-7 (evidence, submission and assessment).',small=True)
    p('<b>[2] Chapter 6.</b> 17S1_CECZ2002_PPT_Chapter6UMLClassDiagram_V1.0.pdf. Pages 18-25, 30, 33-36, 40 and 42-45: member syntax, visibility, multiplicity, composition, generalisation/realisation and boundary/control/entity stereotypes.',small=True)
    p('<b>[3] Chapter 7.</b> 21S1_CE2002_PPT_Chapter7 UML-SequenceDiagram_V1.0.pptx. Slides 5-8, 10-13 and 20-23: lifelines, activation, creation, calls/replies, references and guarded fragments.',small=True)
    p('<b>[4] Chapter 8.</b> 21S1_CE2002_PPT_Chapter8 ModelOOApp_V1.0.pptx. Slides 3-5, 7-9, 38 and 53: responsibility assignment, collaborations and class-responsibility-collaborator analysis.',small=True)
    p('<b>[5] Chapter 9.</b> 17S1_CECZ2002_PPT_Chapter9DesignPrinciples_V1.0(1).pdf. Pages 12-20, 24-26 and 30-45: managing dependencies, cohesion/coupling, encapsulation and SOLID principles.',small=True)
    p('<b>[6] Implementation and verification.</b> src/main/java/restaurant/, tests/stage1_smoke.py, tests/java/restaurant/tests/Stage1ContractTest.java, stage_1.md, docs/chapter9-rebuild-guide.md and docs/report/diagram-sources/. Local tests and independent CLI runs captured on 5 November 2026.',small=True)
    h('Vector diagram appendices')
    table(['Appendix','Page','Purpose'],[
        ['A - Core class diagram','13','Principal runtime classes, interfaces, inheritance, composition and multiplicities.'],
        ['B - Supporting types','14','Immutable view objects and the relevant enum declarations.'],
        ['C - Sequence scenario map','15','Named interaction references, remaining-turn loop and final result check.'],
        ['D - S1 arrival and order','16','Customer 3\'s creation, seating and Pasta order.'],
        ['E - S2 preparation/service','17','Two-unit preparation, READY timing and manager-prioritised service.'],
        ['F - S3 eating/payment','18','Eating completion, deferred payment, revenue and table release.'],
    ],[0.35,0.09,0.56])
    p('The sequence is one scenario decomposed with Chapter 7\'s ref interaction-use notation. Read C, then D-F, then return to C for turns 13-18 and the result check. Display-only calls and other customers\' repeated interactions are explicitly abbreviated. [3]')
    p('Diagram generation uses official PlantUML 1.2026.9. Editable .puml sources and a local rendering script are included in diagrams/. Larger appendix pages preserve label size and vector text; they are intended for on-screen review or large-format printing.',small=True)


def body_footer(canv,doc):
    w,h=A4
    canv.saveState()
    canv.setStrokeColor(BLUE)
    canv.setLineWidth(1.2)
    canv.line(48,h-39,w-48,h-39)
    canv.setFont('Arial-Bold',8)
    canv.setFillColor(NAVY)
    canv.drawString(48,h-29,'RESTAURANT RUSH')
    canv.setFont('Arial',8)
    canv.setFillColor(MUTED)
    canv.drawRightString(w-48,h-29,'SC2002  |  Stage 1 report')
    canv.setStrokeColor(LINE)
    canv.setLineWidth(0.5)
    canv.line(48,37,w-48,37)
    canv.setFont('Arial',7.5)
    canv.drawString(48,24,'5 November 2026  |  Stage 1 scope; final Stage 3 evidence still required')
    canv.drawRightString(w-48,24,str(doc.page))
    canv.restoreState()


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--build-dir',type=Path,required=True)
    args=parser.parse_args()
    build=args.build_dir.resolve()
    evidence=json.loads((build/'evidence.json').read_text())
    content(evidence)
    body=build/'report-body.pdf'
    doc=SimpleDocTemplate(str(body),pagesize=A4,rightMargin=48,leftMargin=48,topMargin=57,bottomMargin=51,title='Restaurant Rush - Stage 1 Design and Implementation Report',author='Restaurant Rush project')
    doc.build(story,onFirstPage=body_footer,onLaterPages=body_footer)
    reader=PdfReader(body)
    if len(reader.pages)!=12:
        raise RuntimeError(f'Expected 12 body pages, got {len(reader.pages)}; inspect pagination before merging.')
    writer=PdfWriter()
    for page_obj in reader.pages: writer.add_page(page_obj)
    for i,title in enumerate(page_titles): writer.add_outline_item(title,i)
    diagrams=[
        ('A - Core class diagram','stage1-class-diagram'),
        ('B - Supporting view and enum types','stage1-supporting-types'),
        ('C - Sequence scenario map','stage1-sequence-diagram'),
        ('D - Arrival, seating and order','stage1-sequence-01-order'),
        ('E - Preparation and service','stage1-sequence-02-service'),
        ('F - Eating and payment','stage1-sequence-03-payment'),
    ]
    for title,name in diagrams:
        diagram_reader=PdfReader(build/(name+'.pdf'))
        if len(diagram_reader.pages)!=1: raise RuntimeError(f'Unexpected overflow in {name}')
        diagram=diagram_reader.pages[0]
        w=float(diagram.mediabox.width); h=float(diagram.mediabox.height)
        overlay=io.BytesIO()
        c=canvas.Canvas(overlay,pagesize=(w,h))
        c.setFont('Arial-Bold',8)
        c.setFillColor(NAVY)
        c.drawString(40,h-22,'APPENDIX '+title)
        c.setFont('Arial',8)
        c.setFillColor(MUTED)
        c.drawString(40,18,'Restaurant Rush - Stage 1  |  PlantUML vector export')
        c.drawRightString(w-40,18,str(len(writer.pages)+1))
        c.save(); overlay.seek(0)
        diagram.merge_page(PdfReader(overlay).pages[0])
        index=len(writer.pages)
        writer.add_page(diagram)
        writer.add_outline_item('Appendix '+title,index)
    writer.add_metadata({'/Title':'Restaurant Rush - Stage 1 Design and Implementation Report','/Author':'Restaurant Rush project','/Subject':'SC2002 Stage 1 design, Chapter 9 principles, UML and verified evidence','/Keywords':'SC2002, Restaurant Rush, Stage 1, SOLID, PlantUML'})
    output=ROOT/'output/pdf/Restaurant_Rush_Stage1_Report.pdf'
    with output.open('wb') as stream: writer.write(stream)
    (ROOT/'docs/stage1-report.md').write_text('\n\n'.join(markdown)+'\n',encoding='utf-8')
    print(f'Created {output} ({len(writer.pages)} pages)')


if __name__=='__main__': main()
